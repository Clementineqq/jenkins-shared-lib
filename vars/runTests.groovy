def call(Map config = [:]) {
    def action  = config.action  ?: 'all'
    def goImage = config.goImage ?: 'golang:1.25-alpine'

    echo "Running tests: ${action}"

    withDockerContainer(goImage) {
      
        withEnv(['HOME=/tmp', 'GOCACHE=/tmp/go-build', 'GOMODCACHE=/tmp/go-mod']) { // go будет писать кеш в /tmp и траблов с access denied не будет надеюсб)
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
}
