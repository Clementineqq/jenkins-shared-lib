def call(Map config = [:]) {
    def action = config.action ?: 'all'

    echo "Running tests: ${action}"

    if (action == 'install' || action == 'all') {
        sh 'npm ci'
    }
    if (action == 'lint' || action == 'all') {
        sh 'npm run lint'
        sh 'npm run type-check'
    }
    if (action == 'unit' || action == 'all') {
        sh 'npm run test -- --coverage'
    }
}