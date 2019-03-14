def jenkinsfile

def overrides = [
    scriptVersion  : 'v6',
    pipelineScript: 'https://git.aurora.skead.no/scm/ao/aurora-pipeline-scripts.git',
    credentialsId: "github",
    checkstyle : false,
    sonarQube: false,
    deployTo: 'maven-central',
    jiraFiksetIKomponentversjon: true,
    versionStrategy: [
      [ branch: 'master', versionHint: '3' ]
    ]
]

fileLoader.withGit(overrides.pipelineScript,, overrides.scriptVersion) {
   jenkinsfile = fileLoader.load('templates/leveransepakke')
}

jenkinsfile.run(overrides.scriptVersion, overrides)
