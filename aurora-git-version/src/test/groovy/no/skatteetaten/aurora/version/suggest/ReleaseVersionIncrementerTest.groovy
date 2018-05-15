package no.skatteetaten.aurora.version.suggest

import spock.lang.Specification
import spock.lang.Unroll

class ReleaseVersionIncrementerTest extends Specification {

  @Unroll
  def "shall suggest version #expectedVersion for segment #versionSegmentToIncrement and version #versionHint"() {
    given:
      def existingVersions = ["1.0.0", "1.0.1", "1.0.2", "1.1.2-SNAPSHOT", "1.1.1", "1.1.0", "1.2.0", "1.2.1", "1.2.2", "1.2.3-SNAPSHOT", "1.3.0"]

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
      "1.1.2"         | VersionSegment.PATCH      | "1.1.x"        // same as '1.1'
      "1.1.2"         | VersionSegment.PATCH      | "1.1-SNAPSHOT" // same as '1.1'
      "1.0.3"         | VersionSegment.PATCH      | "1.0-SNAPSHOT"
      "1.1.2"         | VersionSegment.PATCH      | "1.1-SNAPSHOT"
      "1.2.3"         | VersionSegment.PATCH      | "1.2-SNAPSHOT"
      "1.3.1"         | VersionSegment.PATCH      | "1.3-SNAPSHOT"
      "1.4.0"         | VersionSegment.PATCH      | "1.4-SNAPSHOT"
      "1.5.0"         | VersionSegment.PATCH      | "1.5-SNAPSHOT"
      "1.3.1"         | VersionSegment.PATCH      | "1-SNAPSHOT"
      "2.0.0"         | VersionSegment.PATCH      | "2-SNAPSHOT"
      "3.0.0"         | VersionSegment.PATCH      | "3-SNAPSHOT"
      "1.4.0"         | VersionSegment.MINOR      | "1.2.2"
      "1.2.10"        | VersionSegment.MINOR      | "1.2.10"
      "1.5.10"        | VersionSegment.MINOR      | "1.5.10"
      "1.4.0"         | VersionSegment.MINOR      | "1.0-SNAPSHOT"
      "1.4.0"         | VersionSegment.MINOR      | "1.1-SNAPSHOT"
      "1.4.0"         | VersionSegment.MINOR      | "1.2-SNAPSHOT"
      "1.4.0"         | VersionSegment.MINOR      | "1.3-SNAPSHOT"
      "1.4.0"         | VersionSegment.MINOR      | "1.4-SNAPSHOT"
      "1.5.0"         | VersionSegment.MINOR      | "1.5-SNAPSHOT"
      "1.4.0"         | VersionSegment.MINOR      | "1-SNAPSHOT"
      "2.0.0"         | VersionSegment.MINOR      | "2-SNAPSHOT"
      "3.0.0"         | VersionSegment.MINOR      | "3-SNAPSHOT"

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
      "0.0.0"         | VersionSegment.PATCH      | "0-SNAPSHOT"
      "0.0.0"         | VersionSegment.PATCH      | "0.0-SNAPSHOT"
      "1.0.0"         | VersionSegment.PATCH      | "1-SNAPSHOT"
      "4.0.0"         | VersionSegment.PATCH      | "4-SNAPSHOT"
      "1.0.0"         | VersionSegment.PATCH      | "1.0-SNAPSHOT"
      "1.4.0"         | VersionSegment.PATCH      | "1.4-SNAPSHOT"
      "1.1.1"         | VersionSegment.PATCH      | "1.1.1-SNAPSHOT"
      "1.4.4"         | VersionSegment.PATCH      | "1.4.4-SNAPSHOT"
      "0.0.0"         | VersionSegment.MINOR      | "0-SNAPSHOT"
      "0.0.0"         | VersionSegment.MINOR      | "0.0-SNAPSHOT"
      "1.0.0"         | VersionSegment.MINOR      | "1-SNAPSHOT"
      "4.0.0"         | VersionSegment.MINOR      | "4-SNAPSHOT"
      "1.0.0"         | VersionSegment.MINOR      | "1.0-SNAPSHOT"
      "1.4.0"         | VersionSegment.MINOR      | "1.4-SNAPSHOT"
      "1.1.1"         | VersionSegment.MINOR      | "1.1.1-SNAPSHOT"
      "1.4.4"         | VersionSegment.MINOR      | "1.4.4-SNAPSHOT"

  }

}
