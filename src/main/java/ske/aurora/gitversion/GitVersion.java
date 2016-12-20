package ske.aurora.gitversion;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitVersion {

    public static class Options {
        public String versionPrefix = "v";
        public boolean fallbackToBranchNameEnv = true;
        public String fallbackVersion = "unknown";
    }

    private final Options options;

    private final Repository repository;

    public static String determineVersion(File gitDir) throws IOException {
        return determineVersion(gitDir, new Options());
    }

    public static String determineVersion(File gitDir, Options options) throws IOException {
        return new GitVersion(gitDir, options).determineVersion();
    }

    private GitVersion(File gitDir, Options options) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.repository = builder.setGitDir(new File(gitDir, ".git"))
            .readEnvironment() // scan environment GIT_* variables
            .setMustExist(true)
            .build();
        this.options = options;
    }

    private String determineVersion() throws IOException {
        ObjectId head = repository.resolve("HEAD");
        Optional<String> currentBranchName = getBranchName();

        Optional<String> versionTagOnHead = getVersionTagOnCommit(head);

        return versionTagOnHead.map(this::getVersionFromVersionTag)
            .orElseGet(() -> currentBranchName.map(this::getVersionFromBranchName).orElse(options.fallbackVersion));
    }

    private String getVersionFromVersionTag(String versionTag) {

        return versionTag.replaceFirst(options.versionPrefix, "");
    }

    private String getVersionFromBranchName(String currentBranchName) {

        return String.format("%s-SNAPSHOT", currentBranchName);
    }

    private Optional<String> getVersionTagOnCommit(ObjectId commit) {

        return repository.getTags().entrySet().stream()
            .filter(entry -> entry.getValue().getObjectId().equals(commit))
            .filter(entry -> entry.getKey().startsWith(options.versionPrefix))
            .map(Map.Entry::getKey)
            .findFirst();
    }

    private Optional<String> getBranchName() throws IOException {

        ObjectId head = repository.resolve("HEAD");
        String currentBranchName = repository.getBranch();

        if (!head.getName().equals(currentBranchName)) {
            return Optional.of(currentBranchName);
        }

        if (options.fallbackToBranchNameEnv) {
            String branchNameFromEnv = System.getenv("BRANCH_NAME");
            if (branchNameFromEnv != null) {
                return Optional.of(branchNameFromEnv);
            }
        }

        RevWalk walk = new RevWalk(repository);
        RevCommit commit = walk.parseCommit(repository.resolve(head.getName() + "^0"));
        Optional<String> branchOption = repository.getAllRefs().entrySet().stream()
            .filter(e -> e.getKey().startsWith(Constants.R_HEADS))
            .filter(e -> {
                try {
                    return walk.isMergedInto(commit, walk.parseCommit(e.getValue().getObjectId()));
                } catch (IOException e1) {
                    return false;
                }
            })
            .map(e -> e.getValue().getName().replaceFirst("refs/heads/", ""))
            .findFirst();
        return branchOption;
    }
}