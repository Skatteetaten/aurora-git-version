package no.skatteetaten.aurora.version.suggest

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Testing the combined outcome of ReleaseVersionEvaluator and ReleaseVersionIncrementer
 * Documenting the combined behaviour
 */
class ReleaseVersionTest extends Specification {

  @Unroll
  def "shall suggest version #expectedVersion for version #versionHint"() {
    given:
      def existingVersions = ["1.1.0", "1.1.1", "1.1.2-SNAPSHOT", "1.2.2", "1.2.1", "1.2.0", "1.3.0"]

    when:
      def versionSegmentToIncrement = ReleaseVersionEvaluator.findVersionSegmentToIncrement(
          versionHint,
          Optional.empty(),
          [],
          [])

      def inferredVersion = ReleaseVersionIncrementer.suggestNextReleaseVersion(
          versionSegmentToIncrement,
          versionHint,
          existingVersions)

    then:
      inferredVersion.toString() == expectedVersion

    where:
      expectedVersion | versionHint
      "1.1.2"         | "1.1.0"
      "1.1.2"         | "1.1.1"
      "1.1.2"         | "1.1"
      "1.0.0"         | "1.0-SNAPSHOT"
      "1.1.2"         | "1.1-SNAPSHOT"
      "1.4.0"         | "1.4-SNAPSHOT"
      "1.4.0"         | "1-SNAPSHOT"
      "2.0.0"         | "2-SNAPSHOT"
  }

  @Unroll
  def "shall suggest version #expectedVersion for version #versionHint for #originatingBranchName, #forcePatchIncrementFor and #forceMinorIncrementFor"() {
    given:
      def existingVersions = ["1.1.0", "1.1.1", "1.1.2-SNAPSHOT", "1.2.2", "1.2.1", "1.2.0", "1.3.0"]

    when:
      def versionSegmentToIncrement = ReleaseVersionEvaluator.findVersionSegmentToIncrement(
          versionHint,
          Optional.of(originatingBranchName),
          forcePatchIncrementFor,
          forceMinorIncrementFor)

      def inferredVersion = ReleaseVersionIncrementer.suggestNextReleaseVersion(
          versionSegmentToIncrement,
          versionHint,
          existingVersions)

    then:
      inferredVersion.toString() == expectedVersion

    where:
      expectedVersion | versionHint    | originatingBranchName | forcePatchIncrementFor | forceMinorIncrementFor
      "1.1.2"         | "1.1-SNAPSHOT" | "feature/some"        | ["feature"]            | []
      "1.4.0"         | "1.1-SNAPSHOT" | "feature/some"        | []                     | ["feature"]
      "1.5.0"         | "1.5-SNAPSHOT" | "feature/some"        | ["feature"]            | []
      "1.5.0"         | "1.5-SNAPSHOT" | "feature/some"        | []                     | ["feature"]
      "1.3.1"         | "1-SNAPSHOT"   | "feature/some"        | ["feature"]            | []
      "1.4.0"         | "1-SNAPSHOT"   | "feature/some"        | []                     | ["feature"]
      "2.0.0"         | "2-SNAPSHOT"   | "feature/some"        | ["feature"]            | []
      "2.0.0"         | "2-SNAPSHOT"   | "feature/some"        | []                     | ["feature"]

  }

}
