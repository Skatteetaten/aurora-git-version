package no.skatteetaten.aurora.version

import no.skatteetaten.aurora.version.suggest.VersionSegment
import spock.lang.Specification

class VersionNumberSuggesterTest extends Specification {

  static String repoFolder = GitRepoHelper.repoFolder
  static String repoOnTag = "$repoFolder/on_tag" // repo on branch master, already tagged as v1.0.0 

  def "Should allow manual tag on non release branch"() {
    given: "Configuration indicating v2.0.0 as next version, but respecting existing tag"
      def opt = new SuggesterOptions()
      opt.gitRepoPath = "$repoFolder/on_manual_tag"

    when: "suggesting next version"
      def versionNumber = VersionNumberSuggester.suggestVersion(opt)

    then: "expect snapshot since semantic release and we are not on release branch"
      versionNumber == "Manual"
  }

  def "If not on release branch do not honor semantic tags"() {
    given: "Configuration indicating v2.0.0 as next version, but respecting existing tag"
      def opt = new SuggesterOptions()
      opt.gitRepoPath = repoOnTag
      opt.branchesToInferReleaseVersionsFor = ["develop"]
      opt.branchesToUseTagsAsVersionsFor = ["develop"]

    when: "suggesting next version"
      def versionNumber = VersionNumberSuggester.suggestVersion(opt)

    then: "expect snapshot since semantic release and we are not on release branch"
      versionNumber == "master-SNAPSHOT"
  }

  def "Use version number from tag"() {
    given: "Configuration indicating v2.0.0 as next version, but respecting existing tag"
      def opt = new SuggesterOptions()
      opt.gitRepoPath = repoOnTag
      opt.branchesToInferReleaseVersionsFor = ["master"]
      opt.branchesToUseTagsAsVersionsFor = ["master"]
      opt.versionHint = "2"

    when: "suggesting next version"
      def versionNumber = VersionNumberSuggester.suggestVersion(opt)

    then: "expect version number from tag used"
      versionNumber == "1.0.0"
  }

  def "Ignore version number from tag when suggesting new major version"() {
    given: "Configuration indicating v2.0.0 as next version"
      def opt = new SuggesterOptions()
      opt.gitRepoPath = repoOnTag
      opt.branchesToInferReleaseVersionsFor = ["master"]
      opt.branchesToUseTagsAsVersionsFor = ["master"]
      opt.versionHint = "2"
      opt.tryDeterminingCurrentVersionFromTagName = false

    when: "suggesting next version"
      def versionNumber = VersionNumberSuggester.suggestVersion(opt)

    then: "expect inferred version to be used"
      versionNumber == "2.0.0"
  }

  def "Ignore version number from tag, fallback to default strategy"() {
    given: "Configuration indicating v1.0.1 as next version"
      def opt = new SuggesterOptions()
      opt.gitRepoPath = repoOnTag
      opt.branchesToInferReleaseVersionsFor = ["master"]
      opt.branchesToUseTagsAsVersionsFor = ["master"]
      opt.versionHint = "1.0"
      opt.tryDeterminingCurrentVersionFromTagName = false

    when: "suggesting next version"
      def versionNumber = VersionNumberSuggester.suggestVersion(opt)

    then: "expect inferred version to be used, as if there where not tag"
      versionNumber == "1.0.1"
  }

  def "Ignore version number from tag, forced increase of minor version"() {
    given: "Configuration indicating v1.0.1 as next version"
      def opt = new SuggesterOptions()
      opt.gitRepoPath = repoOnTag
      opt.branchesToInferReleaseVersionsFor = ["master"]
      opt.branchesToUseTagsAsVersionsFor = ["master"]
      opt.versionHint = "1.0"
      opt.tryDeterminingCurrentVersionFromTagName = false
      opt.forceSegmentIncrementForExistingTag = Optional.of(VersionSegment.MINOR)

    when: "suggesting next version"
      def versionNumber = VersionNumberSuggester.suggestVersion(opt)

    then: "expect inferred version with forced increment of minor segment"
      versionNumber == "1.1.0"
  }

  def "Ignore version number from tag, forced increase of patch version"() {
    given: "Configuration indicating v1.1.0 as next version"
      def opt = new SuggesterOptions()
      opt.gitRepoPath = repoOnTag
      opt.branchesToInferReleaseVersionsFor = ["master"]
      opt.versionHint = "1"
      opt.tryDeterminingCurrentVersionFromTagName = false
      opt.forceSegmentIncrementForExistingTag = Optional.of(VersionSegment.PATCH)

    when: "suggesting next version"
      def versionNumber = VersionNumberSuggester.suggestVersion(opt)

    then: "expect inferred version with forced increment of patch segment"
      versionNumber == "1.0.1"
  }

}
