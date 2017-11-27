package no.skatteetaten.aurora.version;

import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

import no.skatteetaten.aurora.version.git.GitRepo;
import no.skatteetaten.aurora.version.git.GitVersion;
import no.skatteetaten.aurora.version.suggest.ReleaseVersionEvaluator;

public class SimpleVersionSuggester {

    public static String suggestVersion(SuggesterOptions options) {
        return new SimpleVersionSuggester(
            GitRepo.fromDir(options.getGitRepoPath())).suggestVersion(
            options.isFallbackToBranchNameEnv(),
            options.getFallbackBranchNameEnvName(),
            options.getVersionPrefix(),
            options.getBranchesToInferReleaseVersionsFor().stream().findFirst().orElse(null),
            options.getVersionHint());
    }

    private final GitRepo gitRepo;

    public SimpleVersionSuggester(GitRepo gitRepo) {
        this.gitRepo = gitRepo;
    }

    public String suggestVersion(
        boolean fallbackToBranchNameEnv,
        String fallbackBranchNameEnvName,
        String versionPrefix,
        String branchNameVersionIncrease,
        String versionHint) {

        // some other branch / commit is being build for which we should not suggest the next increased version
        // just use <branchName>-SNAPSHOT as the version
        String branchName = gitRepo.getBranchName(fallbackToBranchNameEnv, fallbackBranchNameEnvName).orElse(null);
        if (!branchNameVersionIncrease.equals(branchName)) {
            return GitVersion.getVersionFromBranchName(branchName).getVersion();
        }

        // HEAD commit already tagged, use tag version without increment
        ObjectId headCommit = gitRepo.resolve("HEAD");
        List<String> versionTagsFromCommit = gitRepo.getVersionTagsFromCommit(headCommit, versionPrefix);
        if (!versionTagsFromCommit.isEmpty()) {
            return GitVersion.getMostRecentTag(versionTagsFromCommit)
                .orElseThrow(() -> new RuntimeException("Impossible"));
        }

        // Find the latest tag and increase the version base on that
        List<String> allVersionsFromTags = gitRepo.getAllVersionsFromTags(versionPrefix);
        return new ReleaseVersionEvaluator(versionHint).suggestNextReleaseVersionFrom(allVersionsFromTags).toString();

    }
}
