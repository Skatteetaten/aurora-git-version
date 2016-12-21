package ske.aurora.gitversion

import org.apache.tools.ant.taskdefs.Expand

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

    expect:
      GitVersion.determineVersion(new File("$repoFolder/$repo"), options) == expectedVersion

    where:
      repo               | expectedVersion
      "on_branch"        | "develop-SNAPSHOT"
      "on_tag"           | "1.0.0"
      "on_detached_head" | "develop-SNAPSHOT"
  }

  def "Version from branch name"() {

    given:
      def options = new GitVersion.Options()
      options.versionPrefix = '-SNAPSHOT'
      def version = new GitVersion(new File("$repoFolder/on_branch"), options)

    expect:
      version.getVersionFromBranchName(branchName) == expectedVersion

    where:
      branchName                                                 | expectedVersion
      "master"                                                   | "master-SNAPSHOT"
      "develop"                                                  | "develop-SNAPSHOT"
      "bugfix/AOC-8-dialog-for-a-bekrefte-endring-av-tagversjon" |
          "bugfix_AOC_8_dialog_for_a_bekrefte_endring_av_tagversjon-SNAPSHOT"
  }
}