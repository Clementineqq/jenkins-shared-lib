def call(Map config = [:]) {
    def status = config.status ?: 'unknown'
    def channel = config.channel ?: '#deployments'
    def message = config.message ?: ''

    def color = status == 'SUCCESS' ? '#36a64f' : '#e01e5a'

    slackSend(
        channel: channel,
        color: color,
        message: "${status} *${env.JOB_NAME}* #${env.BUILD_NUMBER}: ${message}"
    )
}