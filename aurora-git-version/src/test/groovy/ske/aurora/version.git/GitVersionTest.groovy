package ske.aurora.version.git

import static no.skatteetaten.aurora.version.git.GitVersion.VersionSource.BRANCH
import static no.skatteetaten.aurora.version.git.GitVersion.VersionSource.TAG

import org.apache.tools.ant.taskdefs.Expand
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import no.skatteetaten.aurora.version.git.GitVersion
import spock.lang.Specification
import spock.lang.Unroll

class GitVersionTest extends Specification {

  static String repoFolder

  def setupSpec() {
    // We need a couple of git repositories to test basic git functionality, but there is no easy way to version
    // control one git repository from within another. So I have just zipped these test repos into an archive
    // and unzip them before each test run.
    def ant = new AntBuilder()
    Expand unzip = ant.unzip(src: "src/test/resources/gitrepos.zip",
        dest: "target/resources",
        overwrite: "true")
    repoFolder = "$unzip.dest/gitrepos"
  }

  @Unroll("#repo")
  def "Produces version from branch or tag name"() {

    given:
      def options = new GitVersion.Options(fallbackToBranchNameEnv: false)

    when:
      def version = GitVersion.determineVersion(new File("$repoFolder/$repo"), options)

    then:
      version.version == expectedVersion
      version.source == versionSource

    where:
      repo               | expectedVersion    | versionSource
      "on_branch"        | "develop-SNAPSHOT" | BRANCH
      "on_tag"           | "1.0.0"            | TAG
      "on_detached_head" | "develop-SNAPSHOT" | BRANCH
  }

  def "Version from branch name"() {

    given:
      def options = new GitVersion.Options()
      options.versionPrefix = '-SNAPSHOT'
      Repository repository = new FileRepositoryBuilder()
          .setGitDir(new File("$repoFolder/on_branch"))
          .build();

      def version = new GitVersion(repository, options)

    expect:
      version.getVersionFromBranchName(branchName).version == expectedVersion

    where:
      branchName                                                 | expectedVersion
      "master"                                                   | "master-SNAPSHOT"
      "develop"                                                  | "develop-SNAPSHOT"
      "bugfix/AOC-8-dialog-for-a-bekrefte-endring-av-tagversjon" |
          "bugfix_AOC_8_dialog_for_a_bekrefte_endring_av_tagversjon-SNAPSHOT"
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