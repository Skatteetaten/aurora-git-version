package no.skatteetaten.aurora.version.suggest

import spock.lang.Specification
import spock.lang.Unroll

class ReleaseVersionEvaluatorTest extends Specification {

  @Unroll
  def "shall suggest segment #expectedVersionSegment to be incremented for version #versionHint for #originatingBranchName and #forceMinorIncrementFor"() {
    when:
      def versionSegmentToIncrement = ReleaseVersionEvaluator.findVersionSegmentToIncrement(
          versionHint,
          Optional.of(originatingBranchName),
          forceMinorIncrementFor)
    then:
      versionSegmentToIncrement == expectedVersionSegment
    where:
      expectedVersionSegment | versionHint | originatingBranchName | forceMinorIncrementFor
      VersionSegment.PATCH   | "1"         | "feature/some"        | []
      VersionSegment.MINOR   | "1"         | "feature/some"        | ["feature"]
      VersionSegment.PATCH   | "1.0"       | "feature/some"        | []
      VersionSegment.PATCH   | "1.0"       | "feature/some"        | ["feature"]
      VersionSegment.PATCH   | "1.0.1"     | "feature/some"        | []
      VersionSegment.PATCH   | "1.0.1"     | "feature/some"        | ["feature"]
      VersionSegment.PATCH   | "1.0"       | "feature/some"        | ["feature"]
      VersionSegment.PATCH   | "1.0"       | "bugfix/some"         | ["feature"]
      VersionSegment.PATCH   | "1.0"       | "bugfix/some"         | ["feature"]
      VersionSegment.PATCH   | "1.0"       | "BUGFIX/some"         | ["feature"]

  }

}
