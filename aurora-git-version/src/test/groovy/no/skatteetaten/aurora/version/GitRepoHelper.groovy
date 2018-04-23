package no.skatteetaten.aurora.version

import org.apache.tools.ant.taskdefs.Expand

class GitRepoHelper {

  // We need a couple of git repositories to test basic git functionality, but there is no easy way to version
  // control one git repository from within another. So I have just zipped these test repos into an archive
  // and unzip them before each test run.

  public static String repoFolder

  static {
    def ant = new AntBuilder()
    Expand unzip = ant.unzip(src: "src/test/resources/gitrepos.zip",
        dest: "target/resources",
        overwrite: "true")
    repoFolder = "$unzip.dest/gitrepos"
  }

}
