package no.skatteetaten.aurora.version.suggest

import spock.lang.Specification

class VersionNumberTest extends Specification {

  def "version number with major, minor and patch is considered a semantic version"() {
    given:
      def version = "1.2.3"
    when:
      def versionNumber = VersionNumber.parse(version)
    then:
      versionNumber.isSemanticVersion() == true
      versionNumber.toString() == "1.2.3"
  }

  def "version number with two adjacent periods is not a semantic version"() {
    given:
      def version = "1..2"
    when:
      def versionNumber = VersionNumber.parse(version)
    then:
      versionNumber.isSemanticVersion() == false
  }

  def "Version number elements support multiple digits"() {
    given:
      def version = "1234567.2345678.2322"
    when:
      def versionNumber = VersionNumber.parse(version)
    then:
      versionNumber.toString() == "1234567.2345678.2322"
  }

  def "Various supported snapshot syntaxes"() {
    when:
      def versionNumber = VersionNumber.parse(givenVersion)
      def versionHint = VersionNumber.parseVersionHint(givenVersion)
    then:
      versionNumber.isSemanticVersion() == false
      versionHint.isSemanticVersion() == false
      versionNumber.toString() == expectedVersionString
      versionHint.toString() == expectedVersionString
    where:
      givenVersion   | expectedVersionString
      "1.2-SNAPSHOT" | "1.2"     // maven syntax
      "1-SNAPSHOT"   | "1"       // maven syntax
      "2.1.x"        | "2.1"
      "2.x.x"        | "2"
  }

  def "Version numbers are naturally sorted by their individual segments"() {
    given:
      def unsortedVersions = [
          VersionNumber.parseVersionHint("8"),
          VersionNumber.parseVersionHint("8.2"),
          VersionNumber.parse("8.2.8"),
          VersionNumber.parse("1.8.9"),
          VersionNumber.parse("8.2.0"),
          VersionNumber.parse("10.0.1")].asImmutable()
    when:
      def sortedVersions = unsortedVersions.toSorted()
    then:
      sortedVersions.collect { it.toString() } == ["1.8.9", "8.2.0", "8.2.8", "8", "8.2", "10.0.1"]
  }

  def "version hints are considered greater than equal semantic version"() {
    given:
      def baseVersion = VersionNumber.parse("3.3.0");
      def snapshotVersion = VersionNumber.parseVersionHint("3.3.0");
    when:
      def difference = baseVersion.compareTo(snapshotVersion);
    then:
      difference == -1
  }

  def "version hints are considered greater than same semantic version with more digits"() {
    given:
      def baseVersion = VersionNumber.parse("3.3.1");
      def snapshotVersion = VersionNumber.parseVersionHint("3.3");
    when:
      def difference = baseVersion.compareTo(snapshotVersion);
    then:
      difference == -1
  }

  def "A shortened non semantic version is still a non semantic version"() {
    given:
      def originalVersion = VersionNumber.parseVersionHint("3.3.1");
    when:
      def shortenedVerison = originalVersion.shorten(2);
    then:
      shortenedVerison.isSemanticVersion() == false
      shortenedVerison.toString() == "3.3"
  }

  def "Shorter version numbers autopads length when adapting to longer version number"() {
    given:
      def releasedVersion = VersionNumber.parse("3.4.0");
      def developmentVersion = VersionNumber.parseVersionHint("3.4");
    when:
      def adaptedVersion = releasedVersion.adaptTo(developmentVersion);
    then:
      adaptedVersion.toString() == "3.4.0";
  }

  def "last version number is incremented"() {
    given:
      def version = VersionNumber.parse("3.2.1");
    when:
      def increasedVersion = version.incrementPatchSegment();
    then:
      increasedVersion.toString() == "3.2.2";
  }

  def "last version number is incremented for non semantic versions"() {
    given:
      def version = VersionNumber.parseVersionHint("3.2");
    when:
      def increasedVersion = version.incrementPatchSegment();
    then:
      increasedVersion.toString() == "3.3";
      increasedVersion.isSemanticVersion() == false
  }

  def "Version numbers are extended with segments with value 0 when unlocked"() {
    given:
      def developmentVersion = VersionNumber.parseVersionHint(givenVersion)
    when:
      def unlockedVersion = developmentVersion.unlockVersion()
    then:
      unlockedVersion.toString() == expectedVersion
    where:
      givenVersion | expectedVersion
      "3.3"        | "3.3.0"
      "3"          | "3.0.0"
  }

  def "Version hint is always treated as non semantic version"() {
    when:
      def versionHint = VersionNumber.parseVersionHint(version)
    then:
      versionHint.toString() == parsedVersionHintAsString
      versionHint.semanticVersion == false
    where:
      version          | parsedVersionHintAsString
      "1"              | "1"
      "1.1"            | "1.1"
      "1.0.1"          | "1.0.1"
      "1"              | "1"
      "1.2"            | "1.2"
      "1.2.3"          | "1.2.3"
  }

  def "example of valid semantic version numbers"() {
    given:
      def versions = [
          "1.0.1",
          "1.1.2",
          "1.1.1",
          "123456789.123456789.123456789",
          "123456789.123456789.123456789",
          "18.19.20"
      ]
    when:
      def versionNumbers = versions.collect { VersionNumber.parse(it) }
    then:
      versionNumbers.findAll { it.semanticVersion }.size() == versions.size();
  }

  def "example of non semantic version numbers"() {
    given:
      def versions = [
          ".1",
          "1.2",
          ".123456789",
          "1..1",
          "123456789..123456789",
          "1..1..1",
          "123456789..123456789..123456789",
          "1..1.1..1",
          "123456789..123456789.123456789..123456789",
          "12.0.0.1",
          "1.2"
      ]
    when:
      def versionNumbers = versions.collect { VersionNumber.parse(it) }
    then:
      versionNumbers.findAll { it.semanticVersion }.size() == 0
  }

}

