package no.skatteetaten.aurora.version.suggest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is a Java implementation of Tommy Bø's original implementation in Groovy that could be found in the
 * aurora-cd maven plugin. It has been reimplemented in Java without changing any of the core logic.
 */
public class ReleaseVersionEvaluator {

    private VersionNumber currentVersion;

    public ReleaseVersionEvaluator(String versionNumber) {
        this.currentVersion = VersionNumber.parse(versionNumber);
    }

    public VersionNumber suggestNextReleaseVersionFrom(List<String> listOfVersions) {
        List<VersionNumber> orderedListOfEligibleVersions = listOfVersions.stream()
            .filter(VersionNumber::isValid)
            .map(VersionNumber::parse)
            .sorted()
            .filter(currentVersion::canBeUsedWhenDeterminingReleaseVersion)
            .collect(Collectors.toList());
        if (orderedListOfEligibleVersions.isEmpty()) {
            return currentVersion.unlockVersion();
        } else {
            return orderedListOfEligibleVersions.get(orderedListOfEligibleVersions.size() - 1).adaptTo(currentVersion)
                .incrementLastSegment();
        }
    }
}
