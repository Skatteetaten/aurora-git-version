package no.skatteetaten.aurora.version.git

import static no.skatteetaten.aurora.version.git.GitVersion.VersionSource.BRANCH
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
      "feature/AOS-4044_søknader_med_feil_inntektsår"                                                                         | "feature_AOS_4044_soeknader_med_feil_inntektsaar-SNAPSHOT"
  }

  def "Version from branches with norwegian letters"() {

    given:
      def options = new GitVersion.Options()
      options.versionPrefix = '-SNAPSHOT'

    def version = new GitVersion(new GitRepo(null), options)

    expect:
      version.getVersionFromBranchName(branchName).version == expectedVersion

    where:
      branchName                                                                                                    | expectedVersion
      "develop"                                                                                                     | "develop-SNAPSHOT"
      "bugfix/ABC-1337-this-is-my-life"                                                                             | "bugfix_ABC_1337_this_is_my_life-SNAPSHOT"
      "fature/DEF-1337-SØKNADER-årets-ærfugeljakt"                                                                  | "fature_DEF_1337_SOEKNADER_aarets_aerfugeljakt-SNAPSHOT"
      "feature/GHI-7331-mår-måker-og-andre-dyr-man-ikke-ønsker-å-ha-i-norsk-natur-pga-lang-navn-på-bancher"         | "feature_GHI_7331_maar_maaker_og_andre_dyr_man_ikke_oen-SNAPSHOT"
  }

  def "Version from branches with norwegian letters without normalization"() {

    given:
      def options = new GitVersion.Options()
      options.versionPrefix = '-SNAPSHOT'
      options.setUseNormalizationForNorwegianLetters(false)

      def version = new GitVersion(new GitRepo(null), options)

    expect:
      version.getVersionFromBranchName(branchName).version == expectedVersion

    where:
      branchName                                                                            | expectedVersion
      "master"                                                                              | "master-SNAPSHOT"
      "feature/DEF-1337-øre-nese-hals"                                                      | "feature_DEF_1337_øre_nese_hals-SNAPSHOT"
      "feature/AAA-1337-Årsaker_til-at-ørnen-kan-fly-og-andre-rase-fakta-fra-ørkenen"       | "feature_AAA_1337_Årsaker_til_at_ørnen_kan_fly_og_andre-SNAPSHOT"
  }

  def "Version from branches with comma"() {

    given:
      def options = new GitVersion.Options()
      options.versionPrefix = '-SNAPSHOT'
      options.setUseNormalizationForNorwegianLetters(false)

      def version = new GitVersion(new GitRepo(null), options)

    expect:
      version.getVersionFromBranchName(branchName).version == expectedVersion

    where:
      branchName                                                                                    | expectedVersion
      "feature/ABC-Navigating-through-snow,-sleet,-wind,-and-darkness-is-a-miserable-way-to-travel" | "feature_ABC_Navigating_through_snow__sleet__wind__and_-SNAPSHOT"
      "feature/,DEF-Snow,-sleet,,,"                                                                 | "feature__DEF_Snow__sleet___-SNAPSHOT"
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