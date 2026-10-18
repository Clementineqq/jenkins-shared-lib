// сборка Docker-образа.
def call(Map config = [:]) {
    
    def imageName  = (config.imageName ?: 'app').toLowerCase() //на всякий случай строчными буквами тк докер требует 
    def imageTag = config.imageTag ?: error("imageTag is required")
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    def context    = config.context    ?: '.'

    echo "Building ${imageName}:${imageTag}"

    sh """
        docker build -f ${dockerfile} -t ${imageName}:${imageTag} ${context}
    """

    env.BUILT_IMAGE = "${imageName}:${imageTag}" //запоминаю собранный образ, шобы следующий шаг pushImage его подхватил

    echo "Built: ${env.BUILT_IMAGE}"
}