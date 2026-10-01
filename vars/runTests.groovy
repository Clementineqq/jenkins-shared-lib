
def call(Map config = [:]) {
    def action  = config.action  ?: 'all'                 // install  lint  unit  all
    def goImage = config.goImage ?: 'golang:1.25-alpine'  // образ с гошкой
    def workDir = config.workDir ?: env.WORKSPACE         // папка с исходниками на агенте

    echo "Running tests: ${action}"

    if (action == 'install' || action == 'all') {
        sh "docker run --rm -v ${workDir}:/src -w /src ${goImage} go mod download" // скачиваем зависимости модуля.

    }
    if (action == 'lint' || action == 'all') {
        sh "docker run --rm -v ${workDir}:/src -w /src ${goImage} go vet ./..." // go vet - встроенный анализ кода на подозрительные места

    }
    if (action == 'unit' || action == 'all') {
        sh "docker run --rm -v ${workDir}:/src -w /src ${goImage} go test ./..." // для будущих юнит тестов
    }
}