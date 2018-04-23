package no.skatteetaten.aurora.version;

import static java.util.Collections.emptyList;

import java.util.List;

/**
 * SuggesterOptions for the <code>{@link VersionNumberSuggester#suggestVersion}</code> method.
 */
public class SuggesterOptions {

    /**
     * The prefix of the tags that are used for indicating a version. Tags that do not have this prefix are ignored.
     */
    private String versionPrefix = "v";

    /**
     * Whether or not we should fall back to the value of a specified environment variable if the branch name
     * cannot be determined from the current git state. This can be useful in situations where the git repository
     * has been shallow cloned or is in detached HEAD state and the current commit is not actually on a particular
     * branch.
     */
    private boolean fallbackToBranchNameEnv = true;

    /**
     * The name of the environment variable we expect will contain the name of the current branch if the branch
     * name cannot be determined from the current git state. Build systems like Jenkins typically sets an
     * environment variable with the name of the current branch.
     */
    private String fallbackBranchNameEnvName = "BRANCH_NAME";

    /**
     * A list of branch names that should have versions inferred based on earlier versions and the
     * <code>versionHint</code> when the version cannot be determined from an existing tag.
     */
    private List<String> branchesToInferReleaseVersionsFor = emptyList();

    /**
     * Whether or not we should use try to use existing tags on the current commit for determining the current version.
     * Setting this to <code>false</code> will always yield a snapshot version.
     */
    private boolean tryDeterminingCurrentVersionFromTagName = true;

    /**
     * A list of branch names that should use the tags of the current commit to determine version. Branches not in this
     * list will always yield snapshot versions. An empty list will use all branches. Use
     * <code>tryDeterminingCurrentVersionFromTagName</code> to disable this feature.
     */
    private List<String> branchesToUseTagsAsVersionsFor = emptyList();

    /**
     * TODO: Document
     */
    private String versionHint = null;

    /**
     * The path of the git repository to use
     */
    private String gitRepoPath;

    /**
     * Use name of the branch last merged originated from to determine next version number,
     * partly overriding the versionHint.
     *
     * Empty list of prefixes in both 'determineVersionNumberBasedOnBranchPrefix' and
     * 'determineVersionNumberBasedOnBranchPrefix' will have the same effect as setting this option to false.
     *
     * Will fall back to using the versionHint if the branch prefix is not determined, or it is not a part of
     * 'minorUpdateBranchPrefix' or 'patchUpdateBranchPrefix'. Which is the same as setting this option to false.
     */
    private boolean determineVersionNumberBasedOnBranchPrefix = false;

    /**
     * Comma separated list of branch prefixes which should step the MINOR part of the version number
     * Empty list means all branches, except for those mentioned in 'patchUpdateBranchPrefix'
     * Only valid if 'determineVersionNumberBasedOnBranchPrefix' is set to true
     */
    private String minorUpdateBranchPrefix = "feature";

    /**
     * Comma separated list of branch prefixes which should step the PATCH part of the version number
     * Empty list means all branches, except for those mentioned in 'minorUpdateBranchPrefix'
     * Only valid if 'determineVersionNumberBasedOnBranchPrefix' is set to true
     */
    private String patchUpdateBranchPrefix = "patch,hotfix,bugfix";

    public List<String> getBranchesToInferReleaseVersionsFor() {
        return branchesToInferReleaseVersionsFor;
    }

    public void setBranchesToInferReleaseVersionsFor(List<String> branchesToInferReleaseVersionsFor) {
        this.branchesToInferReleaseVersionsFor = branchesToInferReleaseVersionsFor;
    }

    public String getVersionPrefix() {
        return versionPrefix;
    }

    public void setVersionPrefix(String versionPrefix) {
        this.versionPrefix = versionPrefix;
    }

    public String getVersionHint() {
        return versionHint;
    }

    public void setVersionHint(String versionHint) {
        this.versionHint = versionHint;
    }

    public boolean isFallbackToBranchNameEnv() {
        return fallbackToBranchNameEnv;
    }

    public void setFallbackToBranchNameEnv(boolean fallbackToBranchNameEnv) {
        this.fallbackToBranchNameEnv = fallbackToBranchNameEnv;
    }

    public String getFallbackBranchNameEnvName() {
        return fallbackBranchNameEnvName;
    }

    public void setFallbackBranchNameEnvName(String fallbackBranchNameEnvName) {
        this.fallbackBranchNameEnvName = fallbackBranchNameEnvName;
    }

    public String getGitRepoPath() {
        return gitRepoPath;
    }

    public void setGitRepoPath(String gitRepoPath) {
        this.gitRepoPath = gitRepoPath;
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

    public boolean isDetermineVersionNumberBasedOnBranchPrefix() {
        return determineVersionNumberBasedOnBranchPrefix;
    }

    public void setDetermineVersionNumberBasedOnBranchPrefix(boolean determineVersionNumberBasedOnBranchPrefix) {
        this.determineVersionNumberBasedOnBranchPrefix = determineVersionNumberBasedOnBranchPrefix;
    }

    public String getMinorUpdateBranchPrefix() {
        return minorUpdateBranchPrefix;
    }

    public void setMinorUpdateBranchPrefix(String minorUpdateBranchPrefix) {
        this.minorUpdateBranchPrefix = minorUpdateBranchPrefix;
    }

    public String getPatchUpdateBranchPrefix() {
        return patchUpdateBranchPrefix;
    }

    public void setPatchUpdateBranchPrefix(String patchUpdateBranchPrefix) {
        this.patchUpdateBranchPrefix = patchUpdateBranchPrefix;
    }
}
