package no.skatteetaten.aurora.version.suggest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReleaseTypeEvaluator {

    private final Pattern refLogMergePattern = Pattern.compile("^merge (.*?): (.*?)$");

    private final boolean isActive;
    private final List<String> patchPrefixList;
    private final List<String> minorPrefixList;

    public ReleaseTypeEvaluator(boolean isActive, String patchPrefixes, String minorPrefixes) {
        this.isActive = isActive;
        this.patchPrefixList = commaSeparatedStringToList(patchPrefixes);
        this.minorPrefixList = commaSeparatedStringToList(minorPrefixes);
    }

    public Optional<VersionSegment> evaluateRefLogComments(List<String> refLogComments) {
        if (!isActive) {
            return Optional.empty(); // functionality is turned off - noting to evaluate
        }
        if (patchPrefixList.isEmpty() && minorPrefixList.isEmpty()) {
            return Optional.empty(); // no branch name matches - noting to evaluate
        }

        Optional<String> originatingBranchName = findOriginatingBranchNameFromRefLogComments(refLogComments);
        if (!originatingBranchName.isPresent()) {
            return Optional.empty(); // unable to determine originating branch name
        }

        if (prefixListContainsBranchName(minorPrefixList, originatingBranchName.get())) {
            return Optional.of(VersionSegment.MINOR);
        }
        if (prefixListContainsBranchName(patchPrefixList, originatingBranchName.get())) {
            return Optional.of(VersionSegment.PATCH);
        }
        return Optional.empty(); // no match found
    }

    private boolean prefixListContainsBranchName(List<String> prefixList, String branchName) {
        return prefixList.stream()
            .filter(prefix -> branchName.startsWith(prefix))
            .findFirst()
            .isPresent();
    }

    private Optional<String> findOriginatingBranchNameFromRefLogComments(List<String> refLogComments) {
        return refLogComments.stream()
            .map(refLogMergePattern::matcher)
            .filter(Matcher::matches)
            .map(matcher -> matcher.group(1))
            .findFirst();
    }

    private List<String> commaSeparatedStringToList(String elements) {
        return Arrays.stream(elements.split(","))
            .map(String::trim)
            .filter(string -> !string.isEmpty())
            .collect(Collectors.toList());
    }
}
