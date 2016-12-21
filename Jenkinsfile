#!/usr/bin/env groovy

def jenkinsfile
fileLoader.withGit('https://git.sits.no/git/scm/ao/aurora-pipeline-scripts.git', 'v1.1.1') {
   jenkinsfile = fileLoader.load('templates/bibliotek')
}

jenkinsfile.run('v1.1.1', 'Maven 3', 'ci_aos', 'ci_aos')