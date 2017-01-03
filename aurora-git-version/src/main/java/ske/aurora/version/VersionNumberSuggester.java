package ske.aurora.version;

import static java.util.Collections.emptyList;

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

public final class VersionNumberSuggester {

    private final Repository repository;

    private final Options options;

    public static String suggestVersion() throws IOException {
        return suggestVersion(new Options());
    }

    public static String suggestVersion(Options options) throws IOException {

        Repository repository = getGitRepository(options.getGitRepoPath());
        return new VersionNumberSuggester(repository, options).suggestVersionInternal();
    }

    private VersionNumberSuggester(Repository repository, Options options) {
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
            GitTools.getBranchName(repository, options.fallbackToBranchNameEnv, options.fallbackBranchNameEnvName);

        String currentBranch = currentBranchOption
            .orElseThrow(() -> new IllegalStateException("Unable to determine name of current branch"));

        return options.branchesToInferReleaseVersionsFor.contains(currentBranch);
    }

    private String getInferredVersion() {

        List<String> versions = getAllVersionsFromTags();
        VersionNumber inferredVersion =
            new ReleaseVersionEvaluator(options.versionHint).suggestNextReleaseVersionFrom(versions);
        return inferredVersion.toString();
    }

    private static GitVersion.Options createGitVersionOptions(Options options) {
        return new GitVersion.Options() {
            {
                setFallbackBranchNameEnvName(options.getFallbackBranchNameEnvName());
                setFallbackToBranchNameEnv(options.isFallbackToBranchNameEnv());
                setVersionPrefix(options.getVersionPrefix());
            }
        };
    }

    private List<String> getAllVersionsFromTags() {
        String versionPrefix = options.versionPrefix;
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

    public static class Options {

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
}
