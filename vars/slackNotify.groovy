import groovy.transform.Field

@Field
private def MSG_COLOR_MAP = [
  'STARTED': 'warning',
  'SUCCESS': 'good',
  'FAILURE': 'danger',
  'ABORTED': '#696969'
]

private def constructMsgBody(Map msgParams) {
  def msg = "*${msgParams.buildStatus}:* Job '${msgParams.jobName}' Build ${msgParams.buildName}"
  if(msgParams.containsKey("buildUser")) {
    msg += "\n User: ${msgParams.buildUser}"
  }
  msg += "\n More info at: ${msgParams.buildUrl}"
  return msg
}

def call(Map notifyParams) {
    def buildStatus = notifyParams.buildStatus ?: 'SUCCESS'

    def buildUser
    wrap([$class: 'BuildUser']) {
      buildUser = notifyParams.buildUser ?: BUILD_USER
    }

    def msgBody = constructMsgBody(buildStatus: notifyParams.buildStatus,
                                        jobName: env.JOB_NAME,
                                        buildName: env.BUILD_DISPLAY_NAME,
                                        buildUser: buildUser,
                                        buildUrl: env.BUILD_URL)

    def msgColor = MSG_COLOR_MAP[buildStatus]

    slackSend(color: msgColor,
              teamDomain: notifyParams.slackTeam,
              message: msgBody,
              tokenCredentialId: notifyParams.slackTokenCredentialId,
              channel: notifyParams.slackChannel,
              botUser: notifyParams.botUser ?: 'false')
}
