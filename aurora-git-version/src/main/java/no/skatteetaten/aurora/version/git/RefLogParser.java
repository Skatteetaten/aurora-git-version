package no.skatteetaten.aurora.version.git;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RefLogParser {

    private static final Pattern MERGE_PATTERN = Pattern.compile("^merge (.*?): (.*?)$");

    private RefLogParser() {
    }

    /**
     * Parses a Git ref log for merge comments and finds the name of the originating branch.
     * Will return the first matching comment in the list.
     */
    public static Optional<String> findOriginatingBranchName(List<String> refLogComments) {
        return refLogComments.stream()
            .map(MERGE_PATTERN::matcher)
            .filter(Matcher::matches)
            .map(matcher -> matcher.group(1))
            .findFirst();
    }
}
