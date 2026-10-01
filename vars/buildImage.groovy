// сборка Docker-образа.
def call(Map config = [:]) {
    
    def imageName  = (config.imageName ?: 'app').toLowerCase() //на всякий случай строчными буквами тк докер требует 
    def imageTag   = config.imageTag  ?: 'latest'
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    def context    = config.context    ?: '.'

    echo "Building ${imageName}:${imageTag}"

    sh """
        docker build -f ${dockerfile} -t ${imageName}:${imageTag} -t ${imageName}:latest ${context}
    """

    env.BUILT_IMAGE = "${imageName}:${imageTag}" //запоминаю собранный образ, шобы следующий шаг pushImage его подхватил

    echo "Built: ${env.BUILT_IMAGE}"
}