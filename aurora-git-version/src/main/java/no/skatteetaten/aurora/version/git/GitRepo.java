package no.skatteetaten.aurora.version.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * Wrapper which hides the annoying IOExceptions
 */
public class GitRepo {

    private final Repository gitRepo;

    public GitRepo(Repository gitRepo) {
        this.gitRepo = gitRepo;
    }

    public static GitRepo fromDir(String gitDir) {
        return fromDir(new File(gitDir, "."));
    }

    public static GitRepo fromDir(File gitDir) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder
                .setGitDir(new File(gitDir, ".git"))
                .readEnvironment() // scan environment GIT_* variables
                .setMustExist(true)
                .build();
            return new GitRepo(repository);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getVersionTagsFromCommit(ObjectId commit, String versionPrefix) {
        if (commit == null) {
            return Collections.emptyList();
        }
        return withRepo(repository -> {
            List<String> tags = new ArrayList<>();
            try (Git git = new Git(repository)) {
                List<Ref> call = git.tagList().call();
                for (Ref ref : call) {
                    ObjectId objectId = ref.getObjectId();
                    Ref peeledRef = repository.peel(ref);
                    if (peeledRef.getPeeledObjectId() != null) {
                        objectId = peeledRef.getPeeledObjectId();
                    }
                    if (!objectId.equals(commit)) {
                        continue;
                    }
                    String tagName = tagNameFromRef(ref);
                    if (tagName.startsWith(versionPrefix)) {
                        tags.add(tagName);
                    }
                }
            }
            return tags;
        });
    }

    /**
     * Get all ref log entries with new id matching the given commit id
     */
    public List<ReflogEntry> getRefLogEntriesForCommit(ObjectId commit) {
        if (commit == null) {
            return Collections.emptyList();
        }
        return withRepo(repository -> {
            try (Git git = new Git(repository)) {
                return git.reflog().call().stream()
                    .filter(reflogEntry -> commit.equals(reflogEntry.getNewId()))
                    .collect(Collectors.toList());
            }
        });
    }

    /**
     * Get the first log entry with an id matching the given commit id
     */
    public Optional<RevCommit> getLogEntryForCommit(ObjectId commit) {
        if (commit == null) {
            return Optional.empty();
        }
        return withRepo(repository -> {
            try (Git git = new Git(repository)) {
                Iterable<RevCommit> revCommitIterable = git.log().call();
                return StreamSupport.stream(revCommitIterable.spliterator(), false)
                    .filter(revCommit -> commit.equals(revCommit.getId()))
                    .findFirst();
            }
        });
    }

    private String tagNameFromRef(Ref ref) {
        String tagNamePrefix = "refs/tags/";
        return ref.getName().replaceFirst(tagNamePrefix, "");
    }

    public ObjectId resolve(String head) {
        return withRepo(repository -> repository.resolve(head));
    }

    /**
     * Determine the name of the current branch. If we are in detached head state, we will fall back to the value of
     * the environment variable <code>fallbackBranchNameEnvName</code> to use as branch name.
     */
    public Optional<String> getBranchName(
        boolean fallbackToBranchNameEnv,
        String fallbackBranchNameEnvName) {

        return withRepo(repository -> {
            ObjectId head = repository.resolve("HEAD");
            String currentBranchName = repository.getBranch();

            boolean isDetachedHead = head.getName().equals(currentBranchName);
            if (!isDetachedHead) {
                return Optional.of(currentBranchName);
            }

            return getBranchNameFromDetachedHead(head, fallbackToBranchNameEnv, fallbackBranchNameEnvName);
        });
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
     */
    public Optional<String> getBranchNameFromDetachedHead(
        ObjectId commitId,
        boolean fallbackToBranchNameEnv,
        String fallbackBranchNameEnvName) {

        return withRepo(repository -> {
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
        });

    }

    public List<String> getAllVersionsFromTags(String prefix) {
        return withRepo(repository -> repository.getTags().entrySet().stream()
            .filter(e -> e.getKey().startsWith(prefix))
            .map(e -> e.getKey().replaceFirst(prefix, ""))
            .collect(Collectors.toList()));
    }

    public List<String> getAllRefLogCommentsForCurrentHead() {
        return getRefLogEntriesForCommit(resolve("HEAD")).stream()
            .map(entry -> entry.getComment())
            .collect(Collectors.toList());
    }

    private <T> T withRepo(NoExceptionFunction<Repository, T> fn) {
        try {
            return fn.apply(gitRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface NoExceptionFunction<I, O> {
        O apply(I in) throws Exception;
    }

}
