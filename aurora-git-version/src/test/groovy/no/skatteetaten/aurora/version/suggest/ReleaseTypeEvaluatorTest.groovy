package no.skatteetaten.aurora.version.suggest

import spock.lang.Specification

class ReleaseTypeEvaluatorTest extends Specification {

  def "shall return optional.empty when deactivated"() {
    given:
      def isActive = false;
      def patchPrefixes = "bugfix";
      def minorPrefixes = "feature";
      def releaseTypeEvaluator = new ReleaseTypeEvaluator(isActive, patchPrefixes, minorPrefixes)
    when:
      def result = releaseTypeEvaluator.evaluateRefLogComments([])
    then:
      result.isPresent() == false
  }

  def "shall return optional.empty if both prefix lists are empty"() {
    given:
      def isActive = true;
      def patchPrefixes = "";
      def minorPrefixes = "";
      def releaseTypeEvaluator = new ReleaseTypeEvaluator(isActive, patchPrefixes, minorPrefixes)
    when:
      def result = releaseTypeEvaluator.evaluateRefLogComments([])
    then:
      result.isPresent() == false
  }

  def "shall return optional.empty if no ref log message indicates merge from a branch"() {
    given:
      def isActive = true;
      def patchPrefixes = "bugfix";
      def minorPrefixes = "feature";
      def releaseTypeEvaluator = new ReleaseTypeEvaluator(isActive, patchPrefixes, minorPrefixes)
    when:
      def result = releaseTypeEvaluator.evaluateRefLogComments(["some other comment"])
    then:
      result.isPresent() == false
  }

  def "shall return optional.empty if branch name does not start with any of the given prefixes"() {
    given:
      def isActive = true;
      def patchPrefixes = "bugfix";
      def minorPrefixes = "feature";
      def releaseTypeEvaluator = new ReleaseTypeEvaluator(isActive, patchPrefixes, minorPrefixes)
    when:
      def result = releaseTypeEvaluator.evaluateRefLogComments(["merge some_branch: Fast-forward"])
    then:
      result.isPresent() == false
  }

  def "shall return MINOR if branch name starts with given minor prefix"() {
    given:
      def isActive = true;
      def patchPrefixes = "bugfix,hotfix";
      def minorPrefixes = "feature";
      def releaseTypeEvaluator = new ReleaseTypeEvaluator(isActive, patchPrefixes, minorPrefixes)
    when:
      def result = releaseTypeEvaluator.evaluateRefLogComments([
          "some other comment",
          "merge feature/PROJ-123-some-feature: Fast-forward",
          "more comments to ignore"])
    then:
      result.get() == VersionSegment.MINOR
  }

  def "shall return PATCH if branch name starts with given patch prefix"() {
    given:
      def isActive = true;
      def patchPrefixes = "bugfix,hotfix";
      def minorPrefixes = "feature";
      def releaseTypeEvaluator = new ReleaseTypeEvaluator(isActive, patchPrefixes, minorPrefixes)
    when:
      def result = releaseTypeEvaluator.evaluateRefLogComments([
          "some other comment",
          "merge hotfix/PROJ-123-some-reported-bug: Fast-forward",
          "more comments to ignore"])
    then:
      result.get() == VersionSegment.PATCH
  }

  def "shall return MINOR if branch name exists in both prefix lists"() {
    // this does not make any sense to specify - but the test is here to document the behaviour
    given:
      def isActive = true;
      def patchPrefixes = "bugfix,hotfix";
      def minorPrefixes = "bugfix,hotfix,feature";
      def releaseTypeEvaluator = new ReleaseTypeEvaluator(isActive, patchPrefixes, minorPrefixes)
    when:
      def result = releaseTypeEvaluator.evaluateRefLogComments([
          "some other comment",
          "merge hotfix/PROJ-123-some-reported-bug: Fast-forward",
          "more comments to ignore"])
    then:
      result.get() == VersionSegment.MINOR
  }

  def "shall trim spaces from the comma separated prefix lists"() {
    given:
      def isActive = true;
      def patchPrefixes = "  bugfix  ,  hotfix  ,   branch with spaces   ,  , feature";
      def minorPrefixes = " other ";
      def releaseTypeEvaluator = new ReleaseTypeEvaluator(isActive, patchPrefixes, minorPrefixes)
    when:
      def result = releaseTypeEvaluator.evaluateRefLogComments([
          "some other comment",
          "merge branch with spaces/PROJ-123-some-feature: Fast-forward",
          "more comments to ignore"])
    then:
      result.get() == VersionSegment.PATCH
  }

}
