package ske.aurora.version.suggest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VersionNumber implements Comparable<VersionNumber> {

    public static final String snapshot_notation = "-SNAPSHOT";

    List<String> versionNumberSegments;

    boolean isSnapshot;

    private VersionNumber(List<String> versionNumberSegments, boolean isSnapshot) {

        this.versionNumberSegments = versionNumberSegments;
        this.isSnapshot = isSnapshot;
    }

    public String toString() {
        return versionNumberSegments.stream().collect(Collectors.joining(".")) + (isSnapshot ? snapshot_notation : "");
    }

    public static boolean isValid(String versionString) {
        String pattern = versionString.contains(snapshot_notation)
            ? "\\d+(.\\d+)*(" + snapshot_notation + ")?"
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
        List<String> segments = Arrays.asList(versionString.replaceAll(snapshot_notation + "$", "").split("\\."));
        return new VersionNumber(segments, versionString.endsWith(snapshot_notation));
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

        if (versionNumberSegments.size() == 3) {
            return new VersionNumber(versionNumberSegments, false);
        }
        List<String> segments = new ArrayList<>(versionNumberSegments);
        for (int i=0; i<3-versionNumberSegments.size(); i++) {
            segments.add("0");
        }
        return new VersionNumber(segments, false);
    }

    public VersionNumber adaptTo(VersionNumber example) {

        int newSize;
        if (example.versionNumberSegments.size() == 3) {
            newSize = versionNumberSegments.size();
        } else {
            newSize = versionNumberSegments.size() - (versionNumberSegments.size() - 3);
        }

        List<String> adaptation = versionNumberSegments.subList(0, newSize);
        for (int i = 0; i < (newSize - adaptation.size()); i++) {
            adaptation.add("0");
        }
        return new VersionNumber(adaptation, false);
    }

    public VersionNumber incrementLastSegment() {
        List<String> newSegments = new ArrayList<>(versionNumberSegments.subList(0, versionNumberSegments.size() - 1));
        Integer lastElement = Integer.parseInt(versionNumberSegments.get(versionNumberSegments.size() - 1));
        lastElement += 1;
        newSegments.add(lastElement.toString());
        return new VersionNumber(newSegments, isSnapshot);
    }
}