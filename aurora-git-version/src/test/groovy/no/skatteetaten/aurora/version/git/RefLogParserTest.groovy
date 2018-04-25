package no.skatteetaten.aurora.version.git

import spock.lang.Specification

class RefLogParserTest extends Specification {

  def "shall be able to find originating branch name"() {
    given:
      def refLogComments = [
          "some other comment",
          "merge hotfix/PROJ-123-some-reported-bug: Fast-forward",
          "more comments to ignore"]
    when:
      def branchName = RefLogParser.findOriginatingBranchName(refLogComments)

    then:
      branchName.get() == "hotfix/PROJ-123-some-reported-bug"
  }

}
