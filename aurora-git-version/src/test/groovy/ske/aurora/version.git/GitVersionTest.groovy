package ske.aurora.version.git

import org.apache.tools.ant.taskdefs.Expand
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import spock.lang.Specification
import spock.lang.Unroll

import static ske.aurora.version.git.GitVersion.VersionSource.BRANCH
import static ske.aurora.version.git.GitVersion.VersionSource.TAG

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
}