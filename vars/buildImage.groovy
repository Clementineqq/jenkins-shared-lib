def call(Map config = [:]) {
    def imageName = config.imageName ?: 'app'
    def imageTag = config.imageTag ?: 'latest'

    echo "Building ${imageName}:${imageTag}"

    sh """
        docker build \
            -t ${imageName}:${imageTag} \
            -t ${imageName}:latest \
            .
    """

    env.BUILT_IMAGE = "${imageName}:${imageTag}"
}