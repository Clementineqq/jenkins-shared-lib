def call(Map config = [:]) {
    def status  = (config.status ?: 'unknown').toUpperCase()
    def channel = config.channel ?: '#deployments'
    def message = config.message ?: ''

    def color = status == 'SUCCESS' ? 'good' : 'danger'
    def text  = "${status} *${env.JOB_NAME}* #${env.BUILD_NUMBER}: ${message}"

    echo text

    try {
        slackSend(channel: channel, color: color, message: text)
    } catch (Throwable err) {
        echo "Slack notify skipped: ${err.message}"
    }
}
