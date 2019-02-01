package no.skatteetaten.aurora.version.suggest

import spock.lang.Specification
import spock.lang.Unroll

class ReleaseVersionIncrementerTest extends Specification {

  @Unroll
  def "shall suggest version #expectedVersion for segment #versionSegmentToIncrement and version #versionHint"() {
    given:
      def existingVersions = ["2.1.2", "2.3.3", "3.0.0", "1.0.0", "1.0.1", "1.0.2",
                              "1.1.1", "1.1.0", "1.2.0", "1.2.1", "1.2.2", "1.3.0+meta"]

    when:
      def inferredVersion = ReleaseVersionIncrementer.suggestNextReleaseVersion(
          versionSegmentToIncrement,
          versionHint,
          existingVersions)

    then:
      inferredVersion.toString() == expectedVersion

    where:
      expectedVersion | versionSegmentToIncrement | versionHint
      "1.0.10"        | VersionSegment.PATCH      | "1.0.10" // start from a non existing version
      "1.0.3"         | VersionSegment.PATCH      | "1.0.2"
      "1.0.3"         | VersionSegment.PATCH      | "1.0"
      "1.1.2"         | VersionSegment.PATCH      | "1.1"
      "1.0.3"         | VersionSegment.PATCH      | "1.0"
      "1.1.2"         | VersionSegment.PATCH      | "1.1"
      "1.2.3"         | VersionSegment.PATCH      | "1.2"
      "1.3.1"         | VersionSegment.PATCH      | "1.3"
      "1.4.0"         | VersionSegment.PATCH      | "1.4"
      "1.5.0"         | VersionSegment.PATCH      | "1.5"
      "1.3.1"         | VersionSegment.PATCH      | "1"
      "2.3.4"         | VersionSegment.PATCH      | "2"
      "3.0.1"         | VersionSegment.PATCH      | "3"
      "4.0.0"         | VersionSegment.PATCH      | "4"
      "1.4.0"         | VersionSegment.MINOR      | "1.2.2"
      "1.4.0"         | VersionSegment.MINOR      | "1.2.10"
      "1.5.10"        | VersionSegment.MINOR      | "1.5.10"
      "1.4.0"         | VersionSegment.MINOR      | "1.0"
      "1.4.0"         | VersionSegment.MINOR      | "1.1"
      "1.4.0"         | VersionSegment.MINOR      | "1.2"
      "1.4.0"         | VersionSegment.MINOR      | "1.3"
      "1.4.0"         | VersionSegment.MINOR      | "1.4"
      "1.5.0"         | VersionSegment.MINOR      | "1.5"
      "1.4.0"         | VersionSegment.MINOR      | "1"
      "1.4.0"         | VersionSegment.MINOR      | "1.2"
      "2.4.0"         | VersionSegment.MINOR      | "2"
      "2.4.0"         | VersionSegment.MINOR      | "2.2"
      "3.1.0"         | VersionSegment.MINOR      | "3"
      "3.1.0"         | VersionSegment.MINOR      | "3.0"
      "4.0.0"         | VersionSegment.MINOR      | "4"
      "4.0.0"         | VersionSegment.MINOR      | "4.0"
  }

  @Unroll
  def "shall suggest version #expectedVersion for segment #versionSegmentToIncrement and version #versionHint when there are no existing versions"() {
    given:
      def existingVersions = []

    when:
      def inferredVersion = ReleaseVersionIncrementer.suggestNextReleaseVersion(
          versionSegmentToIncrement,
          versionHint,
          existingVersions)

    then:
      inferredVersion.toString() == expectedVersion

    where:
      expectedVersion | versionSegmentToIncrement | versionHint
      "0.0.0"         | VersionSegment.PATCH      | "0"
      "0.0.0"         | VersionSegment.PATCH      | "0.0"
      "1.0.0"         | VersionSegment.PATCH      | "1"
      "4.0.0"         | VersionSegment.PATCH      | "4"
      "1.0.0"         | VersionSegment.PATCH      | "1.0"
      "1.4.0"         | VersionSegment.PATCH      | "1.4"
      "1.1.1"         | VersionSegment.PATCH      | "1.1.1"
      "1.4.4"         | VersionSegment.PATCH      | "1.4.4"
      "0.0.0"         | VersionSegment.MINOR      | "0"
      "1.0.0"         | VersionSegment.MINOR      | "1"
      "0.0.0"         | VersionSegment.MINOR      | "0.0"
      "1.0.0"         | VersionSegment.MINOR      | "1"
      "4.0.0"         | VersionSegment.MINOR      | "4"
      "1.0.0"         | VersionSegment.MINOR      | "1.0"
      "1.4.0"         | VersionSegment.MINOR      | "1.4"
      "1.1.1"         | VersionSegment.MINOR      | "1.1.1"
      "1.4.4"         | VersionSegment.MINOR      | "1.4.4"
  }

  def "shall ignore non semantic version tags when determining which version number to suggest"() {
    given:
      def existingVersions = ["1.1", "1.0.0", "1.1.0.202", "1.1.0.ad4ea2f35", "1.1.0-SNAPSHOT", "vfeature-SPAP-218-1-DEV"]
      def versionHint = "1"

    when:
      def inferredVersion = ReleaseVersionIncrementer.suggestNextReleaseVersion(
          VersionSegment.MINOR,
          versionHint,
          existingVersions)

    then:
      inferredVersion.toString() == "1.1.0"

  }

}
