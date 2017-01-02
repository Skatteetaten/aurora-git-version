package ske.aurora.version.git;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public final class GitTools {

    private GitTools() {
    }

    /**
     * Determine the name of the current branch. If we are in detached head state, we will fall back to the value of
     * the environment variable <code>fallbackBranchNameEnvName</code> to use as branch name.
     *
     * @param repository
     * @param fallbackToBranchNameEnv
     * @param fallbackBranchNameEnvName
     * @return
     * @throws IOException
     */
    public static Optional<String> getBranchName(Repository repository,
        boolean fallbackToBranchNameEnv, String fallbackBranchNameEnvName) throws IOException {

        ObjectId head = repository.resolve("HEAD");
        String currentBranchName = repository.getBranch();

        boolean isDetachedHead = head.getName().equals(currentBranchName);
        if (!isDetachedHead) {
            return Optional.of(currentBranchName);
        }

        return getBranchNameFromDetachedHead(repository, head, fallbackToBranchNameEnv, fallbackBranchNameEnvName);
    }

    /**
     * If we are trying to determine the branch name of the current commit when we are in detached head
     * state, we need to resort to either hints or heuristics. This method will first check for the presence of
     * an environment variable called <code>options.fallbackBranchNameEnvName</code> (default BRANCH_NAME). If it
     * exists, its value will be used as branch name (Jenkins sets this environment variable before performing a
     * build).
     * <p>
     * If the environment variable is not set we have to resort to a broad search for the commit. We pick the first
     * branch we find the commit in.
     *
     * @param repository
     * @param commitId
     * @param fallbackToBranchNameEnv
     * @param fallbackBranchNameEnvName
     * @return
     * @throws IOException
     */
    public static Optional<String> getBranchNameFromDetachedHead(Repository repository, ObjectId commitId,
        boolean fallbackToBranchNameEnv, String fallbackBranchNameEnvName) throws IOException {

        if (fallbackToBranchNameEnv) {
            String branchNameFromEnv = System.getenv(fallbackBranchNameEnvName);
            if (branchNameFromEnv != null) {
                return Optional.of(branchNameFromEnv);
            }
        }

        RevWalk walk = new RevWalk(repository);
        RevCommit commit = walk.parseCommit(repository.resolve(commitId.getName() + "^0"));
        return repository.getAllRefs().entrySet().stream()
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
    }
}
