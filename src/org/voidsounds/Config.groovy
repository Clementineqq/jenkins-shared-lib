package org.voidsounds

class Config implements Serializable {
    String appName
    String registry
    String dockerCredentialsId

    Config(Map params) {
        this.appName = params.appName ?: 'voidsounds'
        this.registry = params.registry ?: 'registry.example.com'
        this.dockerCredentialsId = params.dockerCredentialsId ?: 'docker-registry'
    }
}