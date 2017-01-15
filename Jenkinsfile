#!/usr/bin/env groovy

def scriptVersion = 'v2.0.0'
def jenkinsfile
fileLoader.withGit('https://git.sits.no/git/scm/ao/aurora-pipeline-scripts.git', scriptVersion) {
   jenkinsfile = fileLoader.load('templates/bibliotek')
}

jenkinsfile.run(scriptVersion, 'Maven 3', 'aurora-bitbucket')