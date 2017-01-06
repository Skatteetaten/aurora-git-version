package ske.aurora.version.suggest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ske.aurora.version.utils.Integers;

/**
 * This class is a Java implementation of Tommy Bø's original implementation in Groovy that could be found in the
 * aurora-cd maven plugin. It has been reimplemented in Java without changing any of the core logic.
 */
public final class VersionNumber implements Comparable<VersionNumber> {

    public static final String SNAPSHOT_NOTATION = "-SNAPSHOT";

    private List<String> versionNumberSegments;

    private boolean isSnapshot;

    private VersionNumber(List<String> versionNumberSegments, boolean isSnapshot) {

        this.versionNumberSegments = versionNumberSegments;
        this.isSnapshot = isSnapshot;
    }

    public String toString() {
        return versionNumberSegments.stream().collect(Collectors.joining(".")) + (isSnapshot ? SNAPSHOT_NOTATION : "");
    }

    public static boolean isValid(String versionString) {

        if (versionString == null) {
            throw new IllegalArgumentException("version string cannot be null");
        }
        String pattern = versionString.contains(SNAPSHOT_NOTATION)
            ? "\\d+(.\\d+)*(" + SNAPSHOT_NOTATION + ")?"
            : "^(\\d+\\.)(\\d+\\.)(\\d+)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(versionString);
        return m.matches();
    }

    public static VersionNumber parse(String versionString) {

        if (!isValid(versionString)) {
            throw new IllegalArgumentException(
                String.format("the version number %s is not well formatted", versionString));
        }
        List<String> segments = Arrays.asList(versionString.replaceAll(SNAPSHOT_NOTATION + "$", "").split("\\."));
        return new VersionNumber(segments, versionString.endsWith(SNAPSHOT_NOTATION));
    }

    public VersionNumber shorten(int newLength) {

        return new VersionNumber(versionNumberSegments.subList(0, newLength), isSnapshot);
    }

    public int compareTo(VersionNumber other) {

        List<Integer> versionComparison = new ArrayList<>();
        int segmentsToCompare = Math.min(this.versionNumberSegments.size(), other.versionNumberSegments.size());
        for (int i = 0; i < segmentsToCompare; i++) {
            Integer me = Integer.parseInt(this.versionNumberSegments.get(i));
            Integer that = Integer.parseInt(other.versionNumberSegments.get(i));
            versionComparison.add(me.compareTo(that));
        }
        versionComparison.add(Boolean.compare(this.isSnapshot, other.isSnapshot));
        versionComparison.add(Integer.compare(this.versionNumberSegments.size(), other.versionNumberSegments.size()));
        return versionComparison.stream().filter((it) -> it != 0).findFirst().orElse(0);
    }

    public boolean canBeUsedWhenDeterminingReleaseVersion(VersionNumber other) {

        if (other.isSnapshot || !this.isSnapshot) {
            return false;
        }
        if (other.versionNumberSegments.size() > this.versionNumberSegments.size()) {
            other = other.shorten(versionNumberSegments.size());
        }

        List<String> thisSegs = this.versionNumberSegments;
        List<String> otherSegs = other.versionNumberSegments;
        if (thisSegs.size() >= 1 && !thisSegs.get(0).equals(otherSegs.get(0))) {
            return false;
        }
        if (thisSegs.size() >= 2 && !thisSegs.get(1).equals(otherSegs.get(1))) {
            return false;
        }
        return true;
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

    public VersionNumber incrementLastSegment() {

        List<String> newSegments = new ArrayList<>(versionNumberSegments.subList(0, versionNumberSegments.size() - 1));
        Integer lastElement = Integer.parseInt(versionNumberSegments.get(versionNumberSegments.size() - 1));
        lastElement += 1;
        newSegments.add(lastElement.toString());
        return new VersionNumber(newSegments, isSnapshot);
    }

    public List<String> getVersionNumberSegments() {

        return versionNumberSegments;
    }

    public boolean isSnapshot() {

        return isSnapshot;
    }
}
