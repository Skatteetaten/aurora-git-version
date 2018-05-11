package no.skatteetaten.aurora.version.suggest

import spock.lang.Specification
import spock.lang.Unroll

class ReleaseVersionEvaluatorTest extends Specification {

  @Unroll
  def "shall suggest segment #expectedVersionSegment to be incremented for version #versionHint"() {
    when:
      def versionSegmentToIncrement = ReleaseVersionEvaluator.findVersionSegmentToIncrement(
          versionHint,
          Optional.empty(),
          [],
          [])
    then:
      versionSegmentToIncrement == expectedVersionSegment
    where:
      expectedVersionSegment | versionHint
      VersionSegment.PATCH   | "1.2.3"
      VersionSegment.PATCH   | "1.0.0-SNAPSHOT"
      VersionSegment.PATCH   | "1.0.1-SNAPSHOT"
      VersionSegment.PATCH   | "1.2"
      VersionSegment.PATCH   | "1.0-SNAPSHOT"
      VersionSegment.PATCH   | "1.1-SNAPSHOT"
      VersionSegment.MINOR   | "1"
      VersionSegment.MINOR   | "1-SNAPSHOT"
  }

  @Unroll
  def "shall suggest segment #expectedVersionSegment to be incremented for version #versionHint for #originatingBranchName, #forcePatchIncrementFor and #forceMinorIncrementFor"() {
    when:
      def versionSegmentToIncrement = ReleaseVersionEvaluator.findVersionSegmentToIncrement(
          versionHint,
          Optional.of(originatingBranchName),
          forcePatchIncrementFor,
          forceMinorIncrementFor)
    then:
      versionSegmentToIncrement == expectedVersionSegment
    where:
      expectedVersionSegment | versionHint      | originatingBranchName | forcePatchIncrementFor | forceMinorIncrementFor
      VersionSegment.PATCH   | "1-SNAPSHOT"     | "feature/some"        | ["feature"]            | []
      VersionSegment.MINOR   | "1-SNAPSHOT"     | "feature/some"        | []                     | ["feature"]
      VersionSegment.PATCH   | "1.0-SNAPSHOT"   | "feature/some"        | ["feature"]            | []
      VersionSegment.MINOR   | "1.0-SNAPSHOT"   | "feature/some"        | []                     | ["feature"]
      VersionSegment.PATCH   | "1.0.1-SNAPSHOT" | "feature/some"        | ["feature"]            | []
      VersionSegment.MINOR   | "1.0.1-SNAPSHOT" | "feature/some"        | []                     | ["feature"]
      VersionSegment.MINOR   | "1.0-SNAPSHOT"   | "feature/some"        | ["feature"]            | ["feature"]
      VersionSegment.PATCH   | "1.0-SNAPSHOT"   | "bugfix/some"         | ["bugfix", "hotfix"]   | ["feature"]
      VersionSegment.PATCH   | "1.0-SNAPSHOT"   | "bugfix/some"         | ["BUGFIX", "hotfix"]   | ["feature"]
      VersionSegment.PATCH   | "1.0-SNAPSHOT"   | "BUGFIX/some"         | ["bugfix", "hotfix"]   | ["feature"]

  }

}
