package ske.aurora.version.suggest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import ske.aurora.version.git.GitTools;
import ske.aurora.version.git.GitVersion;

public class VersionNumberSuggester {

    private final Repository repository;
    private final Options options;

    public static String suggestVersion() throws IOException {
        return suggestVersion(new Options());
    }

    public static String suggestVersion(Options options) throws IOException {

        Repository repository = getGitRepository();
        return new VersionNumberSuggester(repository, options).suggestVersionInternal();
    }

    private VersionNumberSuggester(Repository repository, Options options) {
        this.repository = repository;
        this.options = options;
    }

    private String suggestVersionInternal() throws IOException {

        GitVersion.Version versionFromGit = getVersionFromGit();
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

    private GitVersion.Version getVersionFromGit() throws IOException {

        GitVersion.Options gitVersionOptions = createGitVersionOptions(options);
        return GitVersion.determineVersion(repository, gitVersionOptions);
    }

    private static GitVersion.Options createGitVersionOptions(Options options) {
        return new GitVersion.Options() {{
            setFallbackBranchNameEnvName(options.getFallbackBranchNameEnvName());
            setFallbackToBranchNameEnv(options.isFallbackToBranchNameEnv());
            setVersionPrefix(options.getVersionPrefix());
        }};
    }

    private List<String> getAllVersionsFromTags() {
        String versionPrefix = options.versionPrefix;
        return repository.getTags().entrySet().stream()
            .filter(e -> e.getKey().startsWith(versionPrefix))
            .map(e -> e.getKey().replaceFirst(versionPrefix, ""))
            .collect(Collectors.toList());
    }

    private static Repository getGitRepository() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.setGitDir(new File("./", ".git"))
            .readEnvironment()
            .setMustExist(true)
            .build();
    }

    public static class Options {

        private List<String> branchesToInferReleaseVersionsFor = Collections.singletonList("master");

        private String versionPrefix = "v";

        private String versionHint = null;

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
    }
}
