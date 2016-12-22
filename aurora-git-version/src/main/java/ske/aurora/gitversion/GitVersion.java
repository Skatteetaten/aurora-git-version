package ske.aurora.gitversion;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitVersion {

    private final Options options;

    private final Repository repository;

    public static Version determineVersion(File gitDir) throws IOException {
        return determineVersion(gitDir, new Options());
    }

    /**
     * Determines a version (typically for an application or software library) by inspecting the current state of
     * the Git metadata.
     * <p>
     * The rules are as follows;
     * <ol>
     * <li>If HEAD is at a tag and the name of that tag starts with <code>options.versionPrefix</code>, use the
     * name of that tag with the prefix removed as the version name.</li>
     * <li>If HEAD is not at a tag, and we are not in detached HEAD state, use the name of the current branch
     * concatenated with <code>options.versionFromBranchNamePostfix</code> as the version name</li>
     * <li>If HEAD is not at a tag, and we are in detached HEAD state, first look for the presence of an environment
     * variable with the name <code>fallbackBranchNameEnvName</code> and use that concatenated with
     * <code>options.versionFromBranchNamePostfix</code> as the version name</li>
     * <li>If the environment variable does not exist, search for a branch with the commit in and use that concatenated
     * with <code>options.versionFromBranchNamePostfix</code> as the version name. The first branch with the commit in
     * it will be used.</li>
     * </ol>
     *
     * @param gitDir
     * @param options
     * @return
     * @throws IOException
     */
    public static Version determineVersion(File gitDir, Options options) throws IOException {
        return new GitVersion(gitDir, options).determineVersion();
    }

    protected GitVersion(File gitDir, Options options) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.repository = builder.setGitDir(new File(gitDir, ".git"))
            .readEnvironment() // scan environment GIT_* variables
            .setMustExist(true)
            .build();
        this.options = options;
    }

    protected Version determineVersion() throws IOException {
        ObjectId head = repository.resolve("HEAD");
        Optional<String> currentBranchName = getBranchName(head);

        Optional<String> versionTagOnHead = getVersionTagOnCommit(head);

        return versionTagOnHead
            .map(this::getVersionFromVersionTag)
            .orElseGet(() -> currentBranchName
                .map(this::getVersionFromBranchName)
                .orElse(new Version(options.fallbackVersion, VersionSource.FALLBACK)));
    }

    protected Version getVersionFromVersionTag(String versionTag) {

        String version = versionTag.replaceFirst(options.versionPrefix, "");
        return new Version(version, VersionSource.TAG);
    }

    protected Version getVersionFromBranchName(String currentBranchName) {

        String versionSafeName = currentBranchName.replaceAll("[\\/-]", "_");
        String version = String.format("%s%s", versionSafeName, options.versionFromBranchNamePostfix);
        return new Version(version, VersionSource.BRANCH);
    }

    protected Optional<String> getVersionTagOnCommit(ObjectId commit) {

        return repository.getTags().entrySet().stream()
            .filter(entry -> entry.getValue().getObjectId().equals(commit))
            .filter(entry -> entry.getKey().startsWith(options.versionPrefix))
            .map(Map.Entry::getKey)
            .findFirst();
    }

    protected Optional<String> getBranchName(ObjectId commitId) throws IOException {

        return getBranchName(this.repository, commitId, options.fallbackToBranchNameEnv,
            options.fallbackBranchNameEnvName);
    }

    /**
     * Determine the name of the current branch. The assumption is that head of the current branch is
     * <code>commitId</code> and that commit will be used to determine if we are in detached head state. If we are
     * in detached head state, we will fall back to the value of the environment variable
     * <code>fallbackBranchNameEnvName</code> to use as branch name.
     *
     * @param repository
     * @param commitId
     * @param fallbackToBranchNameEnv
     * @param fallbackBranchNameEnvName
     * @return
     * @throws IOException
     */
    public static Optional<String> getBranchName(Repository repository, ObjectId commitId,
        boolean fallbackToBranchNameEnv, String fallbackBranchNameEnvName) throws IOException {

        String currentBranchName = repository.getBranch();

        boolean isDetachedHead = commitId.getName().equals(currentBranchName);
        if (!isDetachedHead) {
            return Optional.of(currentBranchName);
        }

        return getBranchNameFromDetachedHead(repository, commitId, fallbackToBranchNameEnv, fallbackBranchNameEnvName);
    }

    /**
     * If we are trying to determine the branch name of the current commit when we are in detached head
     * state, we need to resort to either hints or heuristics. This method will first check for the presence of
     * an environment variable called <code>options.fallbackBranchNameEnvName</code> (default BRANCH_NAME). If it
     * exists, its value will be used as branch name (Jenkins sets this environment variable before performing a
     * build).
     * <p>
     * If the environment variable is not set we have to resort to a broad search for the commit. We pick the first
     * branch we find the commit in.
     *
     * @param repository
     * @param commitId
     * @param fallbackToBranchNameEnv
     * @param fallbackBranchNameEnvName
     * @return
     * @throws IOException
     */
    protected static Optional<String> getBranchNameFromDetachedHead(Repository repository, ObjectId commitId,
        boolean fallbackToBranchNameEnv,
        String fallbackBranchNameEnvName) throws IOException {

        if (fallbackToBranchNameEnv) {
            String branchNameFromEnv = System.getenv(fallbackBranchNameEnvName);
            if (branchNameFromEnv != null) {
                return Optional.of(branchNameFromEnv);
            }
        }

        RevWalk walk = new RevWalk(repository);
        RevCommit commit = walk.parseCommit(repository.resolve(commitId.getName() + "^0"));
        return repository.getAllRefs().entrySet().stream()
            .filter(e -> e.getKey().startsWith(Constants.R_HEADS))
            .filter(e -> {
                try {
                    return walk.isMergedInto(commit, walk.parseCommit(e.getValue().getObjectId()));
                } catch (IOException e1) {
                    return false;
                }
            })
            .map(e -> e.getValue().getName().replaceFirst("refs/heads/", ""))
            .findFirst();
    }

    public enum VersionSource {
        TAG,
        BRANCH,
        FALLBACK
    }

    public static class Version {

        private String version;

        private VersionSource source;

        public Version(String version, VersionSource source) {
            this.version = version;
            this.source = source;
        }

        public String getVersion() {
            return version;
        }

        public VersionSource getSource() {
            return source;
        }
    }

    public static class Options {
        private String versionPrefix = "v";
        private boolean fallbackToBranchNameEnv = true;
        private String fallbackVersion = "unknown";
        private String fallbackBranchNameEnvName = "BRANCH_NAME";
        private String versionFromBranchNamePostfix = "-SNAPSHOT";

        public String getVersionPrefix() {
            return versionPrefix;
        }

        public void setVersionPrefix(String versionPrefix) {
            this.versionPrefix = versionPrefix;
        }

        public boolean isFallbackToBranchNameEnv() {
            return fallbackToBranchNameEnv;
        }

        public void setFallbackToBranchNameEnv(boolean fallbackToBranchNameEnv) {
            this.fallbackToBranchNameEnv = fallbackToBranchNameEnv;
        }

        public String getFallbackVersion() {
            return fallbackVersion;
        }

        public void setFallbackVersion(String fallbackVersion) {
            this.fallbackVersion = fallbackVersion;
        }

        public String getFallbackBranchNameEnvName() {
            return fallbackBranchNameEnvName;
        }

        public void setFallbackBranchNameEnvName(String fallbackBranchNameEnvName) {
            this.fallbackBranchNameEnvName = fallbackBranchNameEnvName;
        }

        public String getVersionFromBranchNamePostfix() {
            return versionFromBranchNamePostfix;
        }

        public void setVersionFromBranchNamePostfix(String versionFromBranchNamePostfix) {
            this.versionFromBranchNamePostfix = versionFromBranchNamePostfix;
        }
    }
}
