
def call(Map config = [:]) {
    def action  = config.action  ?: 'all'
    def goImage = config.goImage ?: 'golang:1.25-alpine'

    echo "Running tests: ${action}"

    withDockerContainer(goImage) {
        if (action == 'install' || action == 'all') {
            sh 'go mod download'
        }
        if (action == 'lint' || action == 'all') {
            sh 'go vet ./...'
        }
        if (action == 'unit' || action == 'all') {
            sh 'go test ./...'
        }
    }
}
