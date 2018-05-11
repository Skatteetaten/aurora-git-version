package no.skatteetaten.aurora.version.suggest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        String currentVersionAsString,
        List<String> existingVersions) {

        VersionNumber versionHint = VersionNumber.parseVersionHint(currentVersionAsString);

        List<VersionNumber> orderedListOfVersions = existingVersions.stream()
            .map(VersionNumber::parse)
            .sorted()
            .collect(Collectors.toList());

        Optional<VersionNumber> eligibleVersion = orderedListOfVersions.stream()
            .filter(versionHint::canBeUsedWhenDeterminingReleaseVersion)
            .reduce((first, second) -> second);

        Optional<VersionNumber> lastRelease = orderedListOfVersions.stream()
            .filter(VersionNumber::isSemanticVersion)
            .reduce((first, second) -> second);

        if (!eligibleVersion.isPresent()) {
            return versionHint.unlockVersion();
        }

        if (useVersionHintAsIs(versionHint, eligibleVersion.get())) {
            return versionHint.unlockVersion();
        }

        if (VersionSegment.MINOR.equals(versionSegmentToIncrement)) {
            return lastRelease
                .map(VersionNumber::incrementMinorSegment)
                .orElse(versionHint.unlockVersion());
        }

        return eligibleVersion.get().incrementPatchSegment();
    }

    private static boolean useVersionHintAsIs(VersionNumber versionHint, VersionNumber eligibleVersion) {
        if (versionHint.getVersionNumberSegments().size() != 3) {
            return false;
        }
        VersionNumber versionHintAsSemanticVersion = VersionNumber.parse(versionHint.toString());
        return versionHintAsSemanticVersion.compareTo(eligibleVersion) > 0;
    }

}
