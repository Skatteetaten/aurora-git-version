package ske.aurora.version;

import static java.util.Collections.emptyList;

import java.util.List;

/**
 * Options for the <code>{@link VersionNumberSuggester.suggestVersion}</code> method.
 */
public class Options {

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
     * TODO: Document
     */
    private String versionHint = null;

    /**
     * The path of the git repository to use
     */
    private String gitRepoPath;

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
}
