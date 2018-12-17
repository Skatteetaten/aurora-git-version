package no.skatteetaten.aurora.version;

import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.revwalk.RevCommit;

import no.skatteetaten.aurora.version.git.GitLogParser;
import no.skatteetaten.aurora.version.git.GitRepo;
import no.skatteetaten.aurora.version.git.GitVersion;
import no.skatteetaten.aurora.version.suggest.ReleaseVersionEvaluator;
import no.skatteetaten.aurora.version.suggest.ReleaseVersionIncrementer;
import no.skatteetaten.aurora.version.suggest.VersionNumber;
import no.skatteetaten.aurora.version.suggest.VersionSegment;

/**
 * Class for suggesting a version (typically an application or library version) based on the state of the current
 * git repository. See the README.md file in the project repository for more information.
 */
public final class VersionNumberSuggester {

    private final GitRepo repository;

    private final SuggesterOptions options;

    private VersionNumberSuggester(GitRepo repository, SuggesterOptions options) {
        this.repository = repository;
        this.options = options;
    }

    public static String suggestVersion() {
        return suggestVersion(new SuggesterOptions());
    }

    public static String suggestVersion(SuggesterOptions options) {
        return new VersionNumberSuggester(GitRepo.fromDir(options.getGitRepoPath()), options).suggestVersionHelper();
    }

    private static GitVersion.Options createGitVersionOptions(SuggesterOptions options) {
        GitVersion.Options o = new GitVersion.Options();
        o.setFallbackBranchNameEnvName(options.getFallbackBranchNameEnvName());
        o.setFallbackToBranchNameEnv(options.isFallbackToBranchNameEnv());
        o.setVersionPrefix(options.getVersionPrefix());
        return o;
    }

    private String suggestVersionHelper() {

        GitVersion.Version versionFromGit = new GitVersion(repository, createGitVersionOptions(options))
            .determineVersion();

        String inferedVersion= getInferredVersion(Optional.empty());

        if (versionFromGit.isFromTag()) {
            if (options.getForceSegmentIncrementForExistingTag().isPresent()) {
                return getInferredVersion(options.getForceSegmentIncrementForExistingTag());
            }
            return versionFromGit.getVersion();
        }

        return getInferredVersion(Optional.empty());
    }

    private String getInferredVersion(Optional<VersionSegment> forceUpdateForVersionSegment) {
        List<String> existingVersions = repository.getAllVersionsFromTags(options.getVersionPrefix());
        Optional<RevCommit> commitLogEntry = repository.getLogEntryForCurrentHead();
        Optional<String> originatingBranchName = GitLogParser.findOriginatingBranchName(commitLogEntry);

        VersionSegment versionSegmentToIncrement = forceUpdateForVersionSegment.orElseGet(() ->
            ReleaseVersionEvaluator.findVersionSegmentToIncrement(originatingBranchName,
                options.getForceMinorIncrementForBranchPrefixes()));

        VersionNumber inferredVersion = ReleaseVersionIncrementer.suggestNextReleaseVersion(
            versionSegmentToIncrement,
            options.getVersionHint(),
            existingVersions);

        return inferredVersion.toString();
    }

}
