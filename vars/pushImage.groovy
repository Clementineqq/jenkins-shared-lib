// Переиспользуемый шаг: публикация Docker-образа в registry.
def call(Map config = [:]) {
    def registry      = config.registry ?: 'ghcr.io'
    def imageName     = (config.imageName ?: env.BUILT_IMAGE?.split(':')[0] ?: 'app').toLowerCase()
    def imageTag      = config.imageTag ?: env.BUILT_IMAGE?.split(':')[1] ?: 'latest'
    def credentialsId = config.credentialsId ?: 'github-registry'

    def fullName = "${registry}/${imageName}:${imageTag}"

    echo "Pushing ${fullName}"

    
    withCredentials([usernamePassword( //данные из дженикнс в env для маскировки в логах
        credentialsId: credentialsId,
        usernameVariable: 'DOCKER_USER',
        passwordVariable: 'DOCKER_PASS'
    )]) {

        sh """
            echo "\$DOCKER_PASS" | docker login ${registry} -u "\$DOCKER_USER" --password-stdin
            docker tag ${imageName}:${imageTag} ${fullName}
            docker push ${fullName}
        """
    }
}