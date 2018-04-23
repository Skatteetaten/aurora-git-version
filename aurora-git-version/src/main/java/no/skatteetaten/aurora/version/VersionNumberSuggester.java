package no.skatteetaten.aurora.version;

import java.util.List;
import java.util.Optional;

import no.skatteetaten.aurora.version.git.GitRepo;
import no.skatteetaten.aurora.version.git.GitVersion;
import no.skatteetaten.aurora.version.suggest.ReleaseTypeEvaluator;
import no.skatteetaten.aurora.version.suggest.ReleaseVersionEvaluator;
import no.skatteetaten.aurora.version.suggest.VersionNumber;

/**
 * Class for suggesting a version (typically an application or library version) based on the state of the current
 * git repository. See the README.md file in the project repository for more information.
 */
public final class VersionNumberSuggester {

    private final GitRepo repository;

    private final SuggesterOptions options;

    public static String suggestVersion() {
        return suggestVersion(new SuggesterOptions());
    }

    public static String suggestVersion(SuggesterOptions options) {
        return new VersionNumberSuggester(GitRepo.fromDir(options.getGitRepoPath()), options).suggestVersionHelper();
    }

    private VersionNumberSuggester(GitRepo repository, SuggesterOptions options) {
        this.repository = repository;
        this.options = options;
    }

    private String suggestVersionHelper() {

        GitVersion.Version versionFromGit = new GitVersion(repository, createGitVersionOptions(options))
            .determineVersion();

        if (shouldInferReleaseVersion(versionFromGit)) {
            return getInferredVersion();
        }
        return versionFromGit.getVersion();
    }

    private boolean shouldInferReleaseVersion(GitVersion.Version versionFromGit) {

        if (versionFromGit.isFromTag()) {
            return false;
        }

        Optional<String> currentBranchOption = repository.getBranchName(
            options.isFallbackToBranchNameEnv(),
            options.getFallbackBranchNameEnvName());

        String currentBranch = currentBranchOption
            .orElseThrow(() -> new IllegalStateException("Unable to determine name of current branch"));

        return options.getBranchesToInferReleaseVersionsFor().contains(currentBranch);
    }

    private String getInferredVersion() {
        List<String> versions = repository.getAllVersionsFromTags(options.getVersionPrefix());

        ReleaseTypeEvaluator releaseTypeEvaluator = new ReleaseTypeEvaluator(
            options.isDetermineVersionNumberBasedOnBranchPrefix(),
            options.getPatchUpdateBranchPrefix(),
            options.getMinorUpdateBranchPrefix());

        ReleaseVersionEvaluator releaseVersionEvaluator = new ReleaseVersionEvaluator(
            options.getVersionHint(),
            releaseTypeEvaluator.evaluateRefLogComments(repository.getAllRefLogCommentsForCurrentHead()));

        VersionNumber inferredVersion = releaseVersionEvaluator.suggestNextReleaseVersionFrom(versions);
        return inferredVersion.toString();
    }

    private static GitVersion.Options createGitVersionOptions(SuggesterOptions options) {
        GitVersion.Options o = new GitVersion.Options();
        o.setFallbackBranchNameEnvName(options.getFallbackBranchNameEnvName());
        o.setFallbackToBranchNameEnv(options.isFallbackToBranchNameEnv());
        o.setVersionPrefix(options.getVersionPrefix());
        o.setBranchesToUseTagsAsVersionsFor(options.getBranchesToUseTagsAsVersionsFor());
        o.setTryDeterminingCurrentVersionFromTagName(options.isTryDeterminingCurrentVersionFromTagName());
        return o;
    }

}
