package no.skatteetaten.aurora.version.git;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.lib.ObjectId;

import no.skatteetaten.aurora.version.utils.Assert;

public class GitVersion {

    private final Options options;

    private final GitRepo repository;

    public static Version determineVersion(File gitDir) {
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
     */
    public static Version determineVersion(File gitDir, Options options) {
        return new GitVersion(GitRepo.fromDir(gitDir), options).determineVersion();
    }

    public GitVersion(GitRepo gitRepo, Options options) {
        Assert.notNull(gitRepo, "Repository cannot be null");
        Assert.notNull(options, "SuggesterOptions cannot be null");
        this.repository = gitRepo;
        this.options = options;
    }

    public Version determineVersion() {
        Optional<String> currentBranchName = getCurrentBranchName();

        ObjectId head = repository.resolve("HEAD");
        Optional<String> versionTagOnHead = getVersionTagOnCommit(head);

        return versionTagOnHead
            .map(this::getVersionFromVersionTag)
            .orElseGet(() -> currentBranchName
                .map(GitVersion::getVersionFromBranchName)
                .orElse(new Version(options.fallbackVersion, VersionSource.FALLBACK)));
    }

    protected Version getVersionFromVersionTag(String versionTag) {
        String version = versionTag.replaceFirst(options.versionPrefix, "");
        return new Version(version, VersionSource.TAG);
    }

    protected Optional<String> getVersionTagOnCommit(ObjectId head) {
        return getMostRecentTag(repository.getVersionTagsFromCommit(head, options.versionPrefix));
    }

    private Optional<String> getCurrentBranchName() {
        return repository.getBranchName(
            options.fallbackToBranchNameEnv,
            options.fallbackBranchNameEnvName);
    }

    public static Version getVersionFromBranchName(String currentBranchName) {
        String versionSafeName = currentBranchName.replaceAll("[\\/-]", "_");
        String version = String.format("%s-SNAPSHOT", versionSafeName);
        return new Version(version, VersionSource.BRANCH);
    }

    public static Optional<String> getMostRecentTag(List<String> tags) {

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
        private String versionPrefix = "v";
        private boolean fallbackToBranchNameEnv = true;
        private String fallbackVersion = "unknown";
        private String fallbackBranchNameEnvName = "BRANCH_NAME";

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

    }
}
