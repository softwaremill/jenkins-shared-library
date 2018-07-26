def call(Map notifyParams) {
    //String buildStatus, String slackChannel, String slackTeam, String slackTokenCredentialId

    buildStatus = notifyParams.buildStatus ?: 'SUCCESS'
    def msg = "${buildStatus}: Job: ${env.JOB_NAME} Build: ${env.BUILD_DISPLAY_NAME} (${env.BUILD_URL})"

    if (notifyParams.buildStatus == 'STARTED') {
        color = 'YELLOW'
        colorCode = '#FFFF00'
    } else if (notifyParams.buildStatus == 'SUCCESS') {
        color = 'GREEN'
        colorCode = '#00FF00'
    } else {
        color = 'RED'
        colorCode = '#FF0000'
    }
    slackSend   color: colorCode,
                teamDomain: notifyParams.slackTeam,
                message: msg,
                tokenCredentialId: notifyParams.slackTokenCredentialId,
                channel: notifyParams.slackChannel
}
