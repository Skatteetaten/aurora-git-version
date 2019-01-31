package no.skatteetaten.aurora.version.suggest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.skatteetaten.aurora.version.utils.Integers;

/**
 * Functionality for parsing, validating and manipulating semantic version numbers
 */
public final class VersionNumber implements Comparable<VersionNumber> {

    private List<String> versionNumberSegments;
    private boolean isSemanticVersion;

    private VersionNumber(List<String> versionNumberSegments, boolean isSemanticVersion) {
        this.versionNumberSegments = versionNumberSegments;
        this.isSemanticVersion = isSemanticVersion;
    }

    /**
     * Parses the given string and builds a version number object.
     * All non-number parts of the version is stripped, this to support any snapshot syntax.
     * The flag 'isSemanticVersion' reveals if the number segments are from a pure semantic version or not.
     */
    public static VersionNumber parse(String versionString) {
        boolean forceNonSemanticVersion = false;
        return parseVersionStringHelper(versionString, forceNonSemanticVersion);
    }

    /**
     * A version hint should never be treated as a pure semantic version, even though it might look like one.
     * Parses the given string and builds a version number object with isSemanticVersion = false.
     */
    public static VersionNumber parseVersionHint(String versionString) {
        boolean forceNonSemanticVersion = true;
        return parseVersionStringHelper(versionString, forceNonSemanticVersion);
    }

    //Why do this and not just add groups to the original regex?
    private static VersionNumber parseVersionStringHelper(String versionString, boolean forceNonSemanticVersion) {

        if (versionString == null) {
            throw new IllegalArgumentException("version string cannot be null");
        }
        boolean isSemanticVersion = forceNonSemanticVersion ? false : isValidSemanticVersion(versionString);
        List<String> segments = Arrays.stream(versionString.split("\\."))
            .flatMap(VersionNumber::handleAndFilterVersionSegment)
            .collect(Collectors.toList());
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("No version number segments found in " + versionString);
        }
        return new VersionNumber(segments, isSemanticVersion);
    }

    public static boolean isValidSemanticVersion(String versionString) {
        // This code is very incomplete regarding semantic versioning, but for our needs it will do i guess
        // see https://github.com/semver/semver/issues/232

        Pattern pattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+?(\\+[0-9a-zA-Z-]+(\\.[0-9a-zA-Z-]+)*)?$");
        Matcher matcher = pattern.matcher(versionString);
        return matcher.matches();
    }

    private static Stream<String> handleAndFilterVersionSegment(String segment) {
        Pattern pattern = Pattern.compile("^(\\d+).*?$");
        Matcher matcher = pattern.matcher(segment);
        if (matcher.matches()) {
            return Stream.of(matcher.group(1));
        }
        return Stream.empty();
    }

    public VersionNumber shorten(int newLength) {
        return new VersionNumber(versionNumberSegments.subList(0, newLength), isSemanticVersion);
    }

    public VersionNumber unlockVersion() {

        List<String> segments = new ArrayList<>(versionNumberSegments);
        Integers.times(3 - versionNumberSegments.size(), () -> segments.add("0"));
        return new VersionNumber(segments, false);
    }

    public VersionNumber adaptTo(VersionNumber example) {

        int newSize = example.versionNumberSegments.size() == 3
            ? versionNumberSegments.size()
            : versionNumberSegments.size() - (versionNumberSegments.size() - 3);

        List<String> adaptation = versionNumberSegments.subList(0, newSize);
        Integers.times((newSize - adaptation.size()), () -> adaptation.add("0"));
        return new VersionNumber(adaptation, false);
    }

    public VersionNumber incrementPatchSegment() {
        List<String> newSegments = new ArrayList<>(versionNumberSegments.subList(0, versionNumberSegments.size() - 1));
        Integer lastElement = Integer.parseInt(versionNumberSegments.get(versionNumberSegments.size() - 1));
        lastElement += 1;
        newSegments.add(lastElement.toString());
        return new VersionNumber(newSegments, isSemanticVersion);
    }

    public VersionNumber incrementMinorSegment() {
        List<String> newSegments = new ArrayList<>(versionNumberSegments);
        Integer minorElement = Integer.parseInt(versionNumberSegments.get(versionNumberSegments.size() - 2));
        minorElement += 1;
        newSegments.set(1, minorElement.toString());
        newSegments.set(2, "0");
        return new VersionNumber(newSegments, isSemanticVersion);
    }

    public List<String> getVersionNumberSegments() {

        return versionNumberSegments;
    }

    public boolean isSemanticVersion() {
        return isSemanticVersion;
    }

    @Override
    public int compareTo(VersionNumber other) {

        List<Integer> versionComparison = new ArrayList<>();
        int segmentsToCompare = Math.min(this.versionNumberSegments.size(), other.versionNumberSegments.size());
        for (int i = 0; i < segmentsToCompare; i++) {
            Integer me = Integer.parseInt(this.versionNumberSegments.get(i));
            Integer that = Integer.parseInt(other.versionNumberSegments.get(i));
            versionComparison.add(me.compareTo(that));
        }
        versionComparison.add(Boolean.compare(other.isSemanticVersion, this.isSemanticVersion));
        versionComparison.add(Integer.compare(this.versionNumberSegments.size(), other.versionNumberSegments.size()));
        return versionComparison.stream().filter(it -> it != 0).findFirst().orElse(0);
    }

    @Override
    public String toString() {
        return versionNumberSegments.stream().collect(Collectors.joining("."));
    }
}
