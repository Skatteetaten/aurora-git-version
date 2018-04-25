package no.skatteetaten.aurora.version.suggest;

import java.util.List;
import java.util.Optional;

public final class ReleaseVersionEvaluator {

    private ReleaseVersionEvaluator() {
    }

    /**
     * Evaluates the current version and which segment of it to suggest an update for.
     * According to http://semver.org/
     * <p>
     * If none of the forced update conditions apply, the number of version segments in the current
     * SNAPSHOT version will dictate which segment to increment.<br>
     *   Examples:<br>
     *     1.0-SNAPSHOT - Increment PATCH to next patch version<br>
     *     1-SNAPSHOT   - Increment MINOR to next minor version<br>
     */
    public static VersionSegment findVersionSegmentToIncrement(
        String currentVersionAsString,
        Optional<String> originatingBranchName,
        List<String> forcePatchIncrementForBranchPrefixes,
        List<String> forceMinorIncrementForBranchPrefixes) {

        VersionNumber currentVersion = VersionNumber.parse(currentVersionAsString);

        if (prefixListContainsBranchName(originatingBranchName, forceMinorIncrementForBranchPrefixes)) {
            return VersionSegment.MINOR;
        }
        if (prefixListContainsBranchName(originatingBranchName, forcePatchIncrementForBranchPrefixes)) {
            return VersionSegment.PATCH;
        }

        if (currentVersion.getVersionNumberSegments().size() == 1) {
            return VersionSegment.MINOR;
        } else {
            return VersionSegment.PATCH;
        }
    }

    private static boolean prefixListContainsBranchName(Optional<String> optionalBranchName, List<String> prefixList) {
        return optionalBranchName
            .flatMap(branchName ->
                prefixList.stream()
                    .filter(branchName::startsWith)
                    .findFirst())
            .isPresent();
    }

}

