package ske.aurora.version.suggest;

import java.util.List;
import java.util.stream.Collectors;

public class ReleaseVersionEvaluator {

    private VersionNumber currentVersion;

    public ReleaseVersionEvaluator(String versionNumber) {
        this.currentVersion = VersionNumber.parse(versionNumber);
    }

    VersionNumber suggestNextReleaseVersionFrom(List<String> listOfVersions) {
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