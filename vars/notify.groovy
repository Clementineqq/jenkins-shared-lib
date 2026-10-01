
def call(Map config = [:]) {

    def status  = (config.status ?: 'unknown').toUpperCase() //шобы success и SUCCESS обрабатывались одинаково
    def channel = config.channel ?: '#deployments'
    def message = config.message ?: ''

    def color = status == 'SUCCESS' ? 'good' : 'danger'
    def text  = "${status} *${env.JOB_NAME}* #${env.BUILD_NUMBER}: ${message}"

    echo text

    try {
        slackSend(channel: channel, color: color, message: text)
    } catch (err) {
        echo "Slack notify skipped (плагин Slack не настроен?): ${err.message}"
    }
}