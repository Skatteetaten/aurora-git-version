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

        VersionNumber currentVersion = VersionNumber.parse(currentVersionAsString);

        List<VersionNumber> orderedListOfVersions = existingVersions.stream()
            .filter(VersionNumber::isValid)
            .map(VersionNumber::parse)
            .sorted()
            .collect(Collectors.toList());

        Optional<VersionNumber> eligibleVersion = orderedListOfVersions.stream()
            .filter(currentVersion::canBeUsedWhenDeterminingReleaseVersion)
            .reduce((first, second) -> second);

        Optional<VersionNumber> lastRelease = orderedListOfVersions.stream()
            .filter(versionNumber -> !versionNumber.isSnapshot())
            .reduce((first, second) -> second);

        if (!eligibleVersion.isPresent()) {
            return currentVersion.unlockVersion();
        }

        if (VersionSegment.MINOR.equals(versionSegmentToIncrement)) {
            return lastRelease
                .map(VersionNumber::incrementMinorSegment)
                .orElse(currentVersion.unlockVersion());
        }

        return eligibleVersion.get().incrementPatchSegment();
    }

}
