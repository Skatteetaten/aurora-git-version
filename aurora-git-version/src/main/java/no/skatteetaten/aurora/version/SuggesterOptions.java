package no.skatteetaten.aurora.version;

import static java.util.Collections.emptyList;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.skatteetaten.aurora.version.suggest.VersionSegment;

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
     * Version hint indicating current release track.
     * Can contain non numeric information, as in 1.0-SNAPSHOT normally used by Maven.
     * <p>
     * Examples: <br>
     *  1   - increment MINOR to next minor version for given major track 1.x
     *  1.2 - increment PATCH to next patch version for given minor track 1.2.x
     */
    private String versionHint = null;

    /**
     * The path of the git repository to use
     */
    private String gitRepoPath;

    /**
     * List of branch prefixes which shall force increment of MINOR segment in version number
     */
    private List<String> forceMinorIncrementForBranchPrefixes = Collections.emptyList();
    /**
     * By default, if HEAD of current branch has a tag, that tag vil be used as the suggested version.
     * This option will turn of that feature and force an increment of given version segment when a tag is found.
     *
     * Normally used to allow re-build in CI/CD pipelines with automatic version increment.
     *
     * Note: Value of `tryDeterminingCurrentVersionFromTagName` is ignored when this option is set to a branch prefix.
     */
    private Optional<VersionSegment> forceSegmentIncrementForExistingTag = Optional.empty();


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

    public List<String> getForceMinorIncrementForBranchPrefixes() {
        return forceMinorIncrementForBranchPrefixes;
    }

    public void setForceMinorIncrementForBranchPrefixes(List<String> forceMinorIncrementForBranchPrefixes) {
        this.forceMinorIncrementForBranchPrefixes = forceMinorIncrementForBranchPrefixes;
    }


    public Optional<VersionSegment> getForceSegmentIncrementForExistingTag() {
        return forceSegmentIncrementForExistingTag;
    }

    public void setForceSegmentIncrementForExistingTag(Optional<VersionSegment> forceSegmentIncrementForExistingTag) {
        this.forceSegmentIncrementForExistingTag = forceSegmentIncrementForExistingTag;
    }

}
