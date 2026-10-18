package org.voidsounds

class Config implements Serializable {
    String appName
    String registry
    String dockerCredentialsId

    Config(Map params) {
        this.appName = params.appName ?: 'Clementineqq/voidsounds'
        this.registry = params.registry ?: 'ghcr.io'
        this.dockerCredentialsId = params.dockerCredentialsId ?: 'docker-registry'
    }
}