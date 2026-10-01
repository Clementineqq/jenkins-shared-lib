def call(Map config = [:]) {
    def action  = config.action  ?: 'all'
    def goImage = config.goImage ?: 'golang:1.25-alpine'
    def volume  = config.jenkinsHomeVolume ?: 'jenkins_home'
    def relWs   = env.WORKSPACE.replaceFirst('^/var/jenkins_home/?', '')

    def runInGo = { String cmd ->
        sh "docker run --rm -v ${volume}:/jh -w /jh/${relWs} ${goImage} ${cmd}"
    }

    echo "Running tests: ${action}"

    if (action == 'install' || action == 'all') { runInGo 'go mod download' }
    if (action == 'lint'    || action == 'all') { runInGo 'go vet ./...' }
    if (action == 'unit'    || action == 'all') { runInGo 'go test ./...' }
}
