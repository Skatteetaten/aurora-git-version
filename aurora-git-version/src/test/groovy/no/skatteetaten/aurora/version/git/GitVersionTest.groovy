package no.skatteetaten.aurora.version.git

import static no.skatteetaten.aurora.version.git.GitVersion.VersionSource.BRANCH
import static no.skatteetaten.aurora.version.git.GitVersion.VersionSource.MANUAL_TAG
import static no.skatteetaten.aurora.version.git.GitVersion.VersionSource.TAG

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import no.skatteetaten.aurora.version.GitRepoHelper
import spock.lang.Specification
import spock.lang.Unroll

class GitVersionTest extends Specification {

  static String repoFolder = GitRepoHelper.repoFolder

  @Unroll("#repo")
  def "Produces version from branch or tag name"() {

    given:
      def options = new GitVersion.Options(
              fallbackToBranchNameEnv: false,
              branchesToUseTagsAsVersionsFor: tagBranches,
              tryDeterminingCurrentVersionFromTagName: useTags
      )

    when:
      def version = GitVersion.determineVersion(new File("$repoFolder/$repo"), options)

    then:
      version.version == expectedVersion
      version.source == versionSource

    where:
      repo               | expectedVersion    | versionSource | useTags | tagBranches
      "on_manual_tag"    | "Manual"           | TAG           | true    | []
      "on_branch"        | "develop-SNAPSHOT" | BRANCH        | true    | []
      "on_tag"           | "master-SNAPSHOT"  | BRANCH        | true    | []
      "on_detached_head" | "develop-SNAPSHOT" | BRANCH        | true    | []
      "on_tag"           | "master-SNAPSHOT"  | BRANCH        | false   | []
      "on_tag"           | "master-SNAPSHOT"  | BRANCH        | true    | ['develop']
      "on_tag"           | "1.0.0"            | TAG           | true    | ['master']
  }

  def "Version from branch name"() {

    given:
      def options = new GitVersion.Options()
      options.versionPrefix = '-SNAPSHOT'
      Repository repository = new FileRepositoryBuilder()
          .setGitDir(new File("$repoFolder/on_branch"))
          .build()

      def version = new GitVersion(new GitRepo(repository), options)

    expect:
      version.getVersionFromBranchName(branchName).version == expectedVersion

    where:
      branchName                                                                                                              | expectedVersion
      "master"                                                                                                                | "master-SNAPSHOT"
      "develop"                                                                                                               | "develop-SNAPSHOT"
      "bugfix/AOC-8-dialog-for-a-bekrefte-endring-tagversjon"                                                                 | "bugfix_AOC_8_dialog_for_a_bekrefte_endring_tagversjon-SNAPSHOT"
      "bugfix/AOC-8-dialog-for-a-bekrefte-endring-av-tagversjon-som-er-for-langt-branchnavn-til-at-det-gir-fornuftig-version" | "bugfix_AOC_8_dialog_for_a_bekrefte_endring_av_tagversj-SNAPSHOT"
  }

  def "Get most recent tag with no tags"() {

    expect:
      GitVersion.getMostRecentTag([]) == Optional.empty()
  }

  def "Get most recent tag"() {

    given:
      def tags = [
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-1-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-10-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-11-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-12-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-13-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-14-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-15-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-16-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-17-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-18-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-19-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-2-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-3-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-4-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-5-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-6-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-7-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-8-DEV",
          "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-9-DEV",
          "vfeature/SAF-2190-jenkins-bytte-versjon-av-aurora-cd-1-DEV"
      ]

    expect:
      GitVersion.getMostRecentTag(tags).orElse("") == "vfeature-SAF-2190-jenkins-bytte-versjon-av-aurora-cd-19-DEV"
  }
}