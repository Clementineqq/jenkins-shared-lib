def call(Map config = [:]) {
    def registry = config.registry
    def imageName = config.imageName ?: env.BUILT_IMAGE?.split(':')[0]
    def imageTag = config.imageTag ?: env.BUILT_IMAGE?.split(':')[1] ?: 'latest'

    def fullName = "${registry}/${imageName}:${imageTag}"

    echo "Pushing ${fullName}"

    withCredentials([usernamePassword(
        credentialsId: config.credentialsId ?: 'docker-registry',
        usernameVariable: 'DOCKER_USER',
        passwordVariable: 'DOCKER_PASS'
    )]) {
        sh """
            echo $DOCKER_PASS | docker login ${registry} -u $DOCKER_USER --password-stdin
            docker tag ${imageName}:${imageTag} ${fullName}
            docker push ${fullName}
        """
    }
}