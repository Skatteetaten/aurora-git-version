package no.skatteetaten.aurora.version.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import no.skatteetaten.aurora.version.utils.Assert;

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
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File(gitDir, ".git"))
            .readEnvironment() // scan environment GIT_* variables
            .setMustExist(true)
            .build();
        return determineVersion(repository, options);
    }

    public static Version determineVersion(Repository repository, Options options) throws IOException {
        return new GitVersion(repository, options).determineVersion();
    }

    protected GitVersion(Repository repository, Options options) {
        Assert.notNull(repository, "Repository cannot be null");
        Assert.notNull(options, "SuggesterOptions cannot be null");
        this.repository = repository;
        this.options = options;
    }

    protected Version determineVersion() throws IOException {
        Optional<String> currentBranchName = getCurrentBranchName();

        ObjectId head = repository.resolve("HEAD");
        Optional<String> versionTagOnHead = getVersionTagOnCommit(head);

        return versionTagOnHead
            .map(this::getVersionFromVersionTag)
            .orElseGet(() -> currentBranchName
                .map(this::getVersionFromBranchName)
                .orElse(new Version(options.fallbackVersion, VersionSource.FALLBACK)));
    }

    protected Version getVersionFromVersionTag(String versionTag) {

        String versionName = versionTag.replaceFirst(options.versionPrefix, "");
        return createVersion(VersionSource.TAG, versionName, "");
    }

    protected Version getVersionFromBranchName(String currentBranchName) {

        return createVersion(VersionSource.BRANCH, currentBranchName, options.versionFromBranchNamePostfix);
    }

    protected Version createVersion(VersionSource versionSource, String versionName, String postfix) {

        Assert.notNull(versionSource, "VersionSource cannot be null");
        Assert.notNull(versionName, "VersionName cannot be null");

        int versionNameMaxLength = options.getVersionMaxLength() - (postfix == null ? 0 : postfix.length());
        int startIndex = Math.min(versionName.length(), versionNameMaxLength);

        String versionSafeName = versionName.replaceAll("[\\/-]", "_");
        versionSafeName = versionSafeName.substring(0, startIndex);

        String version = String.format("%s%s", versionSafeName, postfix);

        return new Version(version, versionSource);
    }

    protected Optional<String> getVersionTagOnCommit(ObjectId commit) {

        List<String> tags = getVersionTagsFromCommit(commit);
        return getMostRecentTag(tags);
    }

    protected static Optional<String> getMostRecentTag(List<String> tags) {

        tags.sort((s1, s2) -> {
            int lengthComp = Integer.compare(s1.length(), s2.length());
            if (lengthComp != 0) {
                // If the tag names are not the same length, the shortest ones should come first
                return lengthComp;
            }
            // If the tag names are the same length, order them by their natural order
            return s1.compareTo(s2);
        });
        // Now, the tags list will be either empty or ordered by their natural order with the shortest names first.
        // We can now get the last tag, assuming this is the most recent tag. For instance, in a list of tags like
        // the following; dev-1, dev-10, dev-11, dev-2, dev-3..., we will get dev-11 as the most recent tag.
        return tags.isEmpty() ? Optional.empty() : Optional.of(tags.get(tags.size() - 1));
    }

    protected List<String> getVersionTagsFromCommit(ObjectId commit) {

        List<String> tags = new ArrayList<>();
        try (Git git = new Git(repository)) {
            List<Ref> call = git.tagList().call();
            for (Ref ref : call) {
                ObjectId objectId = ref.getObjectId();
                Ref peeledRef = repository.peel(ref);
                if (peeledRef.getPeeledObjectId() != null) {
                    objectId = peeledRef.getPeeledObjectId();
                }
                if (!objectId.equals(commit)) {
                    continue;
                }
                String tagName = tagNameFromRef(ref);
                if (tagName.startsWith(options.versionPrefix)) {
                    tags.add(tagName);
                }
            }
        } catch (GitAPIException e) {
            throw new GitException("A git error occurred while listing tags", e);
        }
        return tags;
    }

    private String tagNameFromRef(Ref ref) {
        String tagNamePrefix = "refs/tags/";
        return ref.getName().replaceFirst(tagNamePrefix, "");
    }

    protected Optional<String> getCurrentBranchName() throws IOException {

        return GitTools
            .getBranchName(this.repository, options.fallbackToBranchNameEnv, options.fallbackBranchNameEnvName);
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

        public boolean isFromTag() {
            return getSource() == VersionSource.TAG;
        }
    }

    public static class Options {

        /**
         * The default max length of generated version strings. This value actually takes onto consideration that
         * the generated version string will be used where the size limits of domain name labels restricts what values
         * can be used. See http://www.freesoft.org/CIE/RFC/1035/9.htm.
         */
        public static final int DEFAULT_VERSION_MAX_LENGTH = 63;

        private String versionPrefix = "v";
        private boolean fallbackToBranchNameEnv = true;
        private String fallbackVersion = "unknown";
        private String fallbackBranchNameEnvName = "BRANCH_NAME";
        private String versionFromBranchNamePostfix = "-SNAPSHOT";
        private int versionMaxLength = DEFAULT_VERSION_MAX_LENGTH;

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

        public int getVersionMaxLength() {
            return versionMaxLength;
        }

        /**
         * Note that overriding the default value for versionMaxLength may render the generated version string an
         * illegal dns label.
         * @param versionMaxLength
         */
        public void setVersionMaxLength(int versionMaxLength) {
            this.versionMaxLength = versionMaxLength;
        }
    }
}
