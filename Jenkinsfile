def jenkinsfile

def overrides = [
    scriptVersion  : 'v7',
    pipelineScript: 'https://git.aurora.skead.no/scm/ao/aurora-pipeline-scripts.git',
    credentialsId: "github",
    checkstyle : false,
    sonarQube: false,
    iqOrganizationName: "Team AOS",
    iqBreakOnUnstable: true,
    iqEmbedded: true,
    openShiftBuild: false,
    groupId: "no.skatteetaten.aurora",
    artifactId: "aurora-git-version-parent",
    deployTo: 'maven-central',
    uploadLeveransepakke: false,
    jiraFiksetIKomponentversjon: true,
    chatRoom: "#aos-notifications",
    versionStrategy: [
      [ branch: 'master', versionHint: '3' ]
    ]
]

fileLoader.withGit(overrides.pipelineScript,, overrides.scriptVersion) {
   jenkinsfile = fileLoader.load('templates/leveransepakke')
}

jenkinsfile.run(overrides.scriptVersion, overrides)
