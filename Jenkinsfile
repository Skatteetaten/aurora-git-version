#!/usr/bin/env groovy

def scriptVersion = 'feature/AOS-711-generere-semantisk-riktige-versjoner'
def jenkinsfile
fileLoader.withGit('https://git.sits.no/git/scm/ao/aurora-pipeline-scripts.git', scriptVersion) {
   jenkinsfile = fileLoader.load('templates/bibliotek')
}

jenkinsfile.run(scriptVersion, 'Maven 3', 'aurora-bitbucket')