package no.skatteetaten.aurora.version.suggest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ReleaseVersionEvaluator {

    private ReleaseVersionEvaluator() {
    }

    /**
     * Evaluates the current version and which segment of it to suggest an update for.
     * According to http://semver.org/
     * <p>
     * If none of the forced update conditions apply, the number of version segments in the current
     * version hint will dictate which segment to increment. <br>
     * <p>
     * Examples:<br>
     * 1.0 - Will never increment Minor even if commit start matches prefix
     * 1   - Increment PATCH to next minor version<br>
     */
    public static VersionSegment findVersionSegmentToIncrement(
        String versionHintAsString,
        Optional<String> originatingBranchName,
        List<String> forceMinorIncrementForBranchPrefixes) {

        VersionNumber versionHint = VersionNumber.parseVersionHint(versionHintAsString);

        if (versionHint.getVersionNumberSegments().size() == 1
            && prefixListContainsBranchNameCaseInsensitive(originatingBranchName,
            forceMinorIncrementForBranchPrefixes)) {
            return VersionSegment.MINOR;
        }

        return VersionSegment.PATCH;
    }

    private static boolean prefixListContainsBranchNameCaseInsensitive(
        Optional<String> optionalBranchName, List<String> prefixList) {

        List<String> lowerCasedPrefixList = prefixList.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        return optionalBranchName
            .map(String::toLowerCase)
            .flatMap(branchName ->
                lowerCasedPrefixList.stream()
                    .filter(branchName::startsWith)
                    .findFirst())
            .isPresent();
    }

}

