package ske.aurora.version;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import ske.aurora.version.git.GitTools;
import ske.aurora.version.git.GitVersion;
import ske.aurora.version.suggest.ReleaseVersionEvaluator;
import ske.aurora.version.suggest.VersionNumber;

/**
 * Class for suggesting a version (typically an application or library version) based on the state of the current
 * git repository. See the README.md file in the project repository for more information.
 */
public final class VersionNumberSuggester {

    private final Repository repository;

    private final SuggesterOptions options;

    public static String suggestVersion() throws IOException {
        return suggestVersion(new SuggesterOptions());
    }

    public static String suggestVersion(SuggesterOptions options) throws IOException {

        Repository repository = getGitRepository(options.getGitRepoPath());
        return new VersionNumberSuggester(repository, options).suggestVersionInternal();
    }

    private VersionNumberSuggester(Repository repository, SuggesterOptions options) {
        this.repository = repository;
        this.options = options;
    }

    private String suggestVersionInternal() throws IOException {

        GitVersion.Version versionFromGit = GitVersion.determineVersion(repository, createGitVersionOptions(options));
        if (shouldInferReleaseVersion(versionFromGit)) {
            return getInferredVersion();
        }
        return versionFromGit.getVersion();
    }

    private boolean shouldInferReleaseVersion(GitVersion.Version versionFromGit) throws IOException {

        if (versionFromGit.isFromTag()) {
            return false;
        }

        Optional<String> currentBranchOption =
            GitTools
                .getBranchName(repository, options.isFallbackToBranchNameEnv(), options.getFallbackBranchNameEnvName());

        String currentBranch = currentBranchOption
            .orElseThrow(() -> new IllegalStateException("Unable to determine name of current branch"));

        return options.getBranchesToInferReleaseVersionsFor().contains(currentBranch);
    }

    private String getInferredVersion() {

        List<String> versions = getAllVersionsFromTags();
        VersionNumber inferredVersion =
            new ReleaseVersionEvaluator(options.getVersionHint()).suggestNextReleaseVersionFrom(versions);
        return inferredVersion.toString();
    }

    private static GitVersion.Options createGitVersionOptions(SuggesterOptions options) {
        return new GitVersion.Options() {
            {
                setFallbackBranchNameEnvName(options.getFallbackBranchNameEnvName());
                setFallbackToBranchNameEnv(options.isFallbackToBranchNameEnv());
                setVersionPrefix(options.getVersionPrefix());
            }
        };
    }

    private List<String> getAllVersionsFromTags() {
        String versionPrefix = options.getVersionPrefix();
        return repository.getTags().entrySet().stream()
            .filter(e -> e.getKey().startsWith(versionPrefix))
            .map(e -> e.getKey().replaceFirst(versionPrefix, ""))
            .collect(Collectors.toList());
    }

    private static Repository getGitRepository(String gitRepoPath) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.setGitDir(new File(gitRepoPath, ".git"))
            .readEnvironment()
            .setMustExist(true)
            .build();
    }
}
