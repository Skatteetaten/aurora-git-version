package no.skatteetaten.aurora.version.git

import org.eclipse.jgit.revwalk.RevCommit

import spock.lang.Specification
import spock.lang.Unroll

class GitLogParserTest extends Specification {

  def commitLogTestEntries = [
      "BitBucket merge commit":
          """
          |Pull request #416: Feature/PROJ_192-branch-name message including spaces
          |    
          |Merge in PROJ/repo from feature/PROJ_192-branch-name to master
          """.stripMargin().trim(),
      "BitBucket merge from fork":
          """
          |Pull request #3: Bugfix/my-local-branch message including spaces
          |    
          |Merge in PROJ/repo from user/repo:bugfix/my-local-branch to master
          """.stripMargin().trim(),
      "Standard Git merge message":
          "Merge branch 'feature/PROJ-123-feature'",
      "Squash merge message":
          "feature/PROJ-124 Foobar"
  ]

  @Unroll
  def "parsing #commitType, shall result in branch name #expectedBranchName"() {
    given:
      def commitMessageToParse = commitLogTestEntries.get(commitType)
      def revCommit = buildCommitRevWithMessage(commitMessageToParse)
    when:
      def actualBranchName = GitLogParser.findOriginatingBranchName(Optional.of(revCommit)).orElseThrow {
          new RuntimeException("No match found, unable to dertermine branch name.")
      }
    then:
      actualBranchName == expectedBranchName
    where:
      commitType                   | expectedBranchName
      "BitBucket merge commit"     | "feature/PROJ_192-branch-name"
      "BitBucket merge from fork"  | "bugfix/my-local-branch"
      "Standard Git merge message" | "feature/PROJ-123-feature"
      "Squash merge message"       | "feature/PROJ-124"

  }

  def buildCommitRevWithMessage(String message) {
    return RevCommit.parse("""
      |tree 929d072c79049917a3fc5cc37310222bc6be1805
      |parent bd4f6dd2b29f9a50c4fa499e990ed5ff2779be57
      |author FirstName LastName <FirstName.LastName@email.com> 1524144672 +0200
      |committer FirstName LastName <FirstName.LastName@email.com> 1524144672 +0200
      |
      |${message}
      |    
      | Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis non odio mauris. 
      | Nunc vel vestibulum lectus. Donec pharetra interdum nunc et viverra. Fusce ac mattis.
      """.stripMargin().trim().bytes)
  }

}
