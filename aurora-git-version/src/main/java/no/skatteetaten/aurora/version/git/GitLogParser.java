package no.skatteetaten.aurora.version.git;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jgit.revwalk.RevCommit;

public final class GitLogParser {

    private GitLogParser() {
    }

    static final Pattern BITBUCKET_MERGE_FROM_PULL_REQUEST = buildMultilinePattern(
        "Pull request #\\d+: .+[\\s\\r\\n]+Merge in .*? from \\S+?\\/\\S+?:(?<branch>\\S+) to \\S+");

    static final Pattern BITBUCKET_MERGE_NODE = buildMultilinePattern(
        "Pull request #\\d+: .+[\\s\\r\\n]+Merge in .*? from (?<branch>\\S+) to \\S+");

    static final Pattern GIT_MERGE = buildMultilinePattern(
        "Merge branch '(?<branch>\\S+)'");

    static final Pattern SQUASH_MERGE = buildMultilinePattern(
        "^(?<branch>\\S+)");

    // The order in this list is highly significant !
    static final List<Pattern> PATTERNS = Arrays.asList(
        BITBUCKET_MERGE_FROM_PULL_REQUEST,
        BITBUCKET_MERGE_NODE,
        GIT_MERGE,
        SQUASH_MERGE);

    /**
     * Tries to find the name of the originating branch for a merge, by applying a set of
     * regular expressions in a fixed order and use the first branch name found, if there are any.
     */
    public static Optional<String> findOriginatingBranchName(Optional<RevCommit> commitLogEntry) {
        if (!commitLogEntry.isPresent()) {
            return Optional.empty();
        }
        return PATTERNS.stream()
            .map(pattern -> pattern.matcher(commitLogEntry.get().getFullMessage()))
            .map(GitLogParser::fetchBranchGroupFromFirstMatch)
            .flatMap(GitLogParser::optionalToStream)
            .findFirst();
    }

    private static Pattern buildMultilinePattern(String pattern) {
        return Pattern.compile(pattern, Pattern.MULTILINE);
    }

    private static <T> Stream<T> optionalToStream(Optional<T> optional) {
        return optional.isPresent() ? Stream.of(optional.get()) : Stream.empty();
    }

    private static Optional<String> fetchBranchGroupFromFirstMatch(Matcher matcher) {
        return matcher.find() ? Optional.of(matcher.group("branch")) : Optional.empty();
    }
}
