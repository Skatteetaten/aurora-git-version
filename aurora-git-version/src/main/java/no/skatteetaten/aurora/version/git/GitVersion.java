package no.skatteetaten.aurora.version.git;

import static java.util.Collections.emptyList;

import static no.skatteetaten.aurora.version.git.GitVersion.VersionSource.TAG;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.lib.ObjectId;

import no.skatteetaten.aurora.version.suggest.VersionNumber;
import no.skatteetaten.aurora.version.utils.Assert;

public class GitVersion {

    private final Options options;

    private final GitRepo repository;

    public GitVersion(GitRepo gitRepo, Options options) {
        Assert.notNull(gitRepo, "Repository cannot be null");
        Assert.notNull(options, "SuggesterOptions cannot be null");
        this.repository = gitRepo;
        this.options = options;
    }

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
     * The default behaviour can be modified with the <code>options</code> object.
     */
    public static Version determineVersion(File gitDir, Options options) {
        return new GitVersion(GitRepo.fromDir(gitDir), options).determineVersion();
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

    public Version determineVersion() {
        Optional<String> currentBranchName = getCurrentBranchName();

        ObjectId head = repository.resolve("HEAD");

        boolean shouldDetermineVersionFromTag = currentBranchName
            .map(options::shouldDetermineVersionFromTag)
            .orElse(false);

        Optional<String> versionTagOnHead = shouldDetermineVersionFromTag
            ? getVersionTagOnCommit(head)
            : Optional.empty();

        return versionTagOnHead
            .map(v -> versionFromTagOrBranchIfNotReleaseBranch(v, currentBranchName))
            .orElseGet(() -> currentBranchName
                .map(this::getVersionFromBranchName)
                .orElse(new Version(options.fallbackVersion, VersionSource.FALLBACK)));
    }

    private Version versionFromTagOrBranchIfNotReleaseBranch(String v, Optional<String> currentBranchName) {
        Version version = getVersionFromVersionTag(v);
        if (version.source == TAG && VersionNumber.isValidSemanticVersion(version.getVersion())) {
            return currentBranchName
                .filter(this::isNotReleaseBranch)
                .map(this::getVersionFromBranchName)
                .orElse(version);

        }
        return version;
    }

    protected Version getVersionFromVersionTag(String versionTag) {
        String version = versionTag.replaceFirst(options.versionPrefix, "");
        return createVersion(TAG, version, "");
    }

    protected Optional<String> getVersionTagOnCommit(ObjectId head) {
        return getMostRecentTag(repository.getVersionTagsFromCommit(head, options.versionPrefix));
    }

    private Optional<String> getCurrentBranchName() {
        return repository.getBranchName(
            options.fallbackToBranchNameEnv,
            options.fallbackBranchNameEnvName);
    }

    public Version getVersionFromBranchName(String branchName) {

        return createVersion(VersionSource.BRANCH, branchName, options.getVersionFromBranchNamePostfix());
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

    private boolean isNotReleaseBranch(String b) {
        return !this.options.branchesToUseTagsAsVersionsFor.contains(b);
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
            return getSource() == TAG;
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

        /**
         * Whether or not we should use try to use existing tags on the current commit for determining the current
         * version. Setting this to <code>false</code> will always yield a snapshot version.
         */
        private boolean tryDeterminingCurrentVersionFromTagName = true;

        /**
         * A list of branch names that should use the tags of the current commit to determine version. Branches not in
         * this list will always become snapshot versions. An empty list will use all branches. Use
         * <code>tryDeterminingCurrentVersionFromTagName</code> to disable this feature.
         */
        private List<String> branchesToUseTagsAsVersionsFor = emptyList();

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

        public boolean isTryDeterminingCurrentVersionFromTagName() {
            return tryDeterminingCurrentVersionFromTagName;
        }

        public void setTryDeterminingCurrentVersionFromTagName(boolean tryDeterminingCurrentVersionFromTagName) {
            this.tryDeterminingCurrentVersionFromTagName = tryDeterminingCurrentVersionFromTagName;
        }

        public List<String> getBranchesToUseTagsAsVersionsFor() {
            return branchesToUseTagsAsVersionsFor;
        }

        public void setBranchesToUseTagsAsVersionsFor(List<String> branchesToUseTagsAsVersionsFor) {
            this.branchesToUseTagsAsVersionsFor = branchesToUseTagsAsVersionsFor;
        }

        public boolean shouldDetermineVersionFromTag(String currentBranchName) {

            if (!tryDeterminingCurrentVersionFromTagName) {
                return false;
            }
            // Empty list means all branches
            if (branchesToUseTagsAsVersionsFor.isEmpty()) {
                return true;
            }
            return branchesToUseTagsAsVersionsFor.contains(currentBranchName);
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
         *
         * @param versionMaxLength
         */
        public void setVersionMaxLength(int versionMaxLength) {
            this.versionMaxLength = versionMaxLength;
        }
    }
}
