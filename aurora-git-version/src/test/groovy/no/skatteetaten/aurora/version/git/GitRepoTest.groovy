package no.skatteetaten.aurora.version.git

import static org.eclipse.jgit.lib.Constants.OBJ_COMMIT

import org.apache.tools.ant.taskdefs.Expand

import no.skatteetaten.aurora.version.GitRepoHelper
import spock.lang.Specification
import spock.lang.Unroll

class GitRepoTest extends Specification {

  static String repoFolder = GitRepoHelper.repoFolder

  @Unroll("#repo")
  def "shall be able to retrieve log entry for a given commit"() {

    given:
      def repoDirectory = "$repoFolder/$repo"
      def gitRepo = GitRepo.fromDir(repoDirectory)

    when:
      def revCommit = gitRepo.getLogEntryForCommit(gitRepo.resolve("HEAD")).get()

    then:
      revCommit.type == type
      revCommit.name == name
      revCommit.fullMessage.trim() == fullMessage

    where:
      repo                          | type       | name                                       | fullMessage
      "on_branch"                   | OBJ_COMMIT | "edf6570e29a70ce9d52f40416ff81cf092b4f19e" | "README.md"
      "on_detached_head"            | OBJ_COMMIT | "7aa95ecd3ec7958bfc0e08b497d2c3c391e3df3f" | "A change"
      "on_master_with_ff_merge"     | OBJ_COMMIT | "d871dbbb25ba836bb31b5b3045bd323fd4f2f68f" | "PROJ-321 fixed the fix me"
      "on_master_with_merge_commit" | OBJ_COMMIT | "533cf3a21561310581ad3ff461924c3a91cb7a1e" | "Merge branch 'feature/PROJ-123-feature'"
      "on_master_without_tag"       | OBJ_COMMIT | "dc31c2d153be0662c18bc3e653c04a29f291d602" | "A change"
      "on_tag"                      | OBJ_COMMIT | "edf6570e29a70ce9d52f40416ff81cf092b4f19e" | "README.md"
  }

  @Unroll("#repo")
  def "shall be able to retrieve ref log entries for a given commit"() {

    given:
      def repoDirectory = "$repoFolder/$repo"
      def gitRepo = GitRepo.fromDir(repoDirectory)

    when:
      def refLogEntries = gitRepo.getRefLogEntriesForCommit(gitRepo.resolve("HEAD"))
      def refLogEntriesComment = refLogEntries.collect { it.comment }

    then:
      refLogEntries.size() == comments.size()
      refLogEntriesComment.containsAll(comments)

    where:
      repo                          | comments
      "on_detached_head"            | ["checkout: moving from develop to 7aa95ecd3ec7958bfc0e08b497d2c3c391e3df3f", "commit: A change"]
      "on_master_with_ff_merge"     | ["checkout: moving from 1ab2be8918ea732d0631fb56bd955506ce2e85fb to master", "merge bugfix/PROJ-321-fix-me: Fast-forward", "commit: PROJ-321 fixed the fix me"]
      "on_master_with_merge_commit" | ["checkout: moving from dd31dba24c531781676336a4cc418d7dde8c956b to master", "merge feature/PROJ-123-feature: Merge made by the 'recursive' strategy."]
      "on_master_without_tag"       | ["commit: A change"]
  }

  @Unroll("#repo")
  def "shall be able to retrieve ref log comments for current head"() {

    given:
      def repoDirectory = "$repoFolder/$repo"
      def gitRepo = GitRepo.fromDir(repoDirectory)

    when:
      def refLogComments = gitRepo.getAllRefLogCommentsForCurrentHead()

    then:
      refLogComments.size() == comments.size()
      refLogComments.containsAll(comments)

    where:
      repo                          | comments
      "on_detached_head"            | ["checkout: moving from develop to 7aa95ecd3ec7958bfc0e08b497d2c3c391e3df3f", "commit: A change"]
      "on_master_with_ff_merge"     | ["checkout: moving from 1ab2be8918ea732d0631fb56bd955506ce2e85fb to master", "merge bugfix/PROJ-321-fix-me: Fast-forward", "commit: PROJ-321 fixed the fix me"]
      "on_master_with_merge_commit" | ["checkout: moving from dd31dba24c531781676336a4cc418d7dde8c956b to master", "merge feature/PROJ-123-feature: Merge made by the 'recursive' strategy."]
      "on_master_without_tag"       | ["commit: A change"]
  }

}
