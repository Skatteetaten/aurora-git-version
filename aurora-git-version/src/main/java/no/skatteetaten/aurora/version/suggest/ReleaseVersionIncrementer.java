package no.skatteetaten.aurora.version.suggest;

import java.util.List;
import java.util.Optional;

public final class ReleaseVersionIncrementer {

    private ReleaseVersionIncrementer() {
    }

    /**
     * Suggests a new version number based on given segment to increase, combined with current version
     * and existing version tags.
     * <p>
     * http://semver.org/
     */
    public static VersionNumber suggestNextReleaseVersion(
        VersionSegment versionSegmentToIncrement,
        String versionHintAsString,
        List<String> existingVersions) {

        VersionNumber versionHint = VersionNumber.parseVersionHint(versionHintAsString);

        Optional<VersionNumber> latestTagInCurrentReleaseTrack = existingVersions.stream()
            .map(VersionNumber::parse)
            .sorted()
            .filter(versionTag ->
                isVersionTagPartOfReleaseTrack(versionSegmentToIncrement, versionHint, versionTag))
            .reduce((first, second) -> second);

        // First version tag in a new release track
        if (!latestTagInCurrentReleaseTrack.isPresent()) {
            return versionHint.unlockVersion();
        }

        // To handle version bumping within the same release track
        if (isVersionHintGraterThanVersionTag(versionHint, latestTagInCurrentReleaseTrack.get())) {
            return versionHint.unlockVersion();
        }

        if (VersionSegment.MINOR.equals(versionSegmentToIncrement)) {
            return latestTagInCurrentReleaseTrack.get().incrementMinorSegment();
        } else {
            return latestTagInCurrentReleaseTrack.get().incrementPatchSegment();
        }
    }

    private static boolean isVersionTagPartOfReleaseTrack(
        VersionSegment versionSegment, VersionNumber versionHint, VersionNumber versionTag) {

        if (!versionTag.isSemanticVersion()) {
            return false;
        }

        List<String> versionTagSegments = versionTag.getVersionNumberSegments();
        List<String> versionHintSegments = versionHint.getVersionNumberSegments();

        int segmentsToCompare = VersionSegment.PATCH.equals(versionSegment) ? 2 : 1;
        if (versionHintSegments.size() < segmentsToCompare) {
            segmentsToCompare = versionHintSegments.size();
        }

        if (segmentsToCompare == 0) {
            return false;
        }
        if (segmentsToCompare >= 1 && !versionHintSegments.get(0).equals(versionTagSegments.get(0))) {
            return false;
        }
        if (segmentsToCompare >= 2 && !versionHintSegments.get(1).equals(versionTagSegments.get(1))) {
            return false;
        }
        return true;
    }

    private static boolean isVersionHintGraterThanVersionTag(VersionNumber versionHint, VersionNumber versionTag) {
        VersionNumber versionHintAsSemanticVersion = VersionNumber.parse(versionHint.unlockVersion().toString());
        return versionHintAsSemanticVersion.compareTo(versionTag) > 0;
    }

}
