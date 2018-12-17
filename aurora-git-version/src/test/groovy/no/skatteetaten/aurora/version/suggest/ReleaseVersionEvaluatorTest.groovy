package no.skatteetaten.aurora.version.suggest

import spock.lang.Specification
import spock.lang.Unroll

class ReleaseVersionEvaluatorTest extends Specification {

  @Unroll
  def "shall suggest segment #expectedVersionSegment to be incremented for version #versionHint for #originatingBranchName, #forcePatchIncrementFor and #forceMinorIncrementFor"() {
    when:
      def versionSegmentToIncrement = ReleaseVersionEvaluator.findVersionSegmentToIncrement(
          Optional.of(originatingBranchName),
          forceMinorIncrementFor)
    then:
      versionSegmentToIncrement == expectedVersionSegment
    where:
      expectedVersionSegment  | originatingBranchName |  forceMinorIncrementFor
      VersionSegment.PATCH    | "feature/some"        |  []
      VersionSegment.MINOR    | "feature/some"        |  ["feature"]
      VersionSegment.PATCH    | "feature/some"        |  []
      VersionSegment.MINOR    | "feature/some"        |  ["feature"]
      VersionSegment.PATCH    | "feature/some"        |  []
      VersionSegment.MINOR    | "feature/some"        |  ["feature"]
      VersionSegment.MINOR    | "feature/some"        |  ["feature"]
      VersionSegment.PATCH    | "bugfix/some"         |  ["feature"]
      VersionSegment.PATCH    | "bugfix/some"         |  ["feature"]
      VersionSegment.PATCH    | "BUGFIX/some"         |  ["feature"]

  }

}
