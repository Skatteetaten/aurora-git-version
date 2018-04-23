package no.skatteetaten.aurora.version.suggest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class is a Java implementation of Tommy Bø's original implementation in Groovy that could be found in the
 * aurora-cd maven plugin. It has been extended with support for forced update of given version segment.
 * <p>
 * http://semver.org/
 */
public class ReleaseVersionEvaluator {

    private final VersionNumber currentVersion;
    private final Optional<VersionSegment> forcedSegment;

    public ReleaseVersionEvaluator(String versionNumber) {
        this.currentVersion = VersionNumber.parse(versionNumber);
        this.forcedSegment = Optional.empty();
    }

    public ReleaseVersionEvaluator(String versionNumber, Optional<VersionSegment> forcedUpdateOfVersionSegment) {
        this.currentVersion = VersionNumber.parse(versionNumber);
        this.forcedSegment = forcedUpdateOfVersionSegment;
    }

    public VersionNumber suggestNextReleaseVersionFrom(List<String> listOfVersions) {
        List<VersionNumber> orderedListOfVersions = listOfVersions.stream()
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

        // forced update of minor version segment, unless hint indicates step in major version number
        if (forcedSegmentIs(VersionSegment.MINOR)) {
            int currentVersionMajor = Integer.parseInt(currentVersion.getVersionNumberSegments().get(0));
            int lastReleaseMajor = lastRelease
                .map(vn -> Integer.parseInt(vn.getVersionNumberSegments().get(0)))
                .orElse(0);
            if (currentVersionMajor > lastReleaseMajor) {
                return currentVersion.unlockVersion();
            }
            return lastRelease
                .map(VersionNumber::incrementMinorSegment)
                .orElse(currentVersion.unlockVersion());
        }

        // eligible version has never been used
        if (!eligibleVersion.isPresent()) {
            return currentVersion.unlockVersion();
        }

        // eligible version has been used, forcing patch update, use next available patch version
        if (forcedSegmentIs(VersionSegment.PATCH)) {
            return eligibleVersion.get().incrementPatchSegment();
        }

        // eligible version has been used, version hint indicating minor should be increased
        if (currentVersion.getVersionNumberSegments().size() == 1) {
            return eligibleVersion.get().incrementMinorSegment();
        }

        // no other rules apply, use next available patch version
        return eligibleVersion.get().incrementPatchSegment();
    }

    private boolean forcedSegmentIs(VersionSegment versionSegment) {
        if (versionSegment == null) {
            return false;
        }
        if (!forcedSegment.isPresent()) {
            return false;
        }
        return versionSegment.equals(forcedSegment.get());
    }

}

