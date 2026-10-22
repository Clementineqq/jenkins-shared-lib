# jenkins-shared-lib

Общая библиотека шагов для Jenkins-пайплайнов. Позволяет не копировать
одинаковые куски из `Jenkinsfile` в `Jenkinsfile`, а собирать пайплайны
из версионированных переиспользуемых шагов.

Подключается так:

```groovy
@Library('jenkins-shared-lib@v1.0.0') _
```

## Структура

```
vars/                        глобальные шаги, доступные как функции
  buildImage.groovy          сборка Docker-образа
  pushImage.groovy           публикация образа в registry
  runTests.groovy            go mod download / vet / test в контейнере
  notify.groovy              уведомление в Slack 
src/org/voidsounds/
  Config.groovy              дефолты проекта (appName, registry, credentials)
```

## Шаги

### `runTests(action: 'all', goImage: 'golang:1.25-alpine')`

Прогоняет шаги внутри контейнера с Go (`withDockerContainer`).
`action`: `install` | `lint` | `unit` | `all`.
`HOME`, `GOCACHE`, `GOMODCACHE` перенаправлены в `/tmp`, иначе на агенте
ловим `permission denied` на кеше.

### `buildImage(imageName:, imageTag:, dockerfile: 'Dockerfile', context: '.')`

Собирает образ и запоминает его в `env.BUILT_IMAGE`, чтобы следующий шаг
не принимал тег повторно. `imageTag` обязателен — шаг упадёт с понятной ошибкой.

### `pushImage(registry: 'ghcr.io', imageName:, imageTag:, credentialsId:)`

Логин в registry через `--password-stdin` (пароль не попадает в лог),
`docker tag`, `docker push`, `docker logout`. По умолчанию берёт имя и тег
из `env.BUILT_IMAGE`.

### `notify(status:, message:, channel: '#deployments', tokenCredentialId:)`

Отправляет сообщение в Slack. Если креденшен не настроен — пишет в лог и
**не валит пайплайн**: уведомления не должны ломать сборку.

## Пример пайплайна

```groovy
@Library('jenkins-shared-lib@v1.0.0') _

pipeline {
    agent { label 'docker' }

    environment {
        APP_NAME  = 'clementineqq/voidsounds'
        REGISTRY  = 'ghcr.io'
        IMAGE_TAG = "${env.GIT_COMMIT.take(7)}"
    }

    stages {
        stage('Test')         { steps { runTests(action: 'all') } }
        stage('Build Image')  { steps { buildImage(imageName: "${APP_NAME}", imageTag: "${IMAGE_TAG}") } }
        stage('Push')         { steps { pushImage(registry: "${REGISTRY}", imageName: "${APP_NAME}", imageTag: "${IMAGE_TAG}", credentialsId: 'github-registry') } }
    }

    post {
        success { notify(status: 'success', message: "Built ${REGISTRY}/${APP_NAME}:${IMAGE_TAG}") }
        failure { notify(status: 'failure', message: 'CI failed') }
    }
}
```

## Версионирование

Шаги ломаются совместимостью так же, как обычный код, поэтому пайплайны
подключаются к **тегом**, а не к ветке:

```bash
git tag v1.1.0
git push origin v1.1.0
```

- `@v1.0.0` — стабильная версия, на неё смотрят боевые пайплайны
- `@dev` — разработка; используется только в тестовых джобах

## Требования к Jenkins

- плагин **Docker Pipeline** (`withDockerContainer`)
- плагин **Slack Notification**
- агент с меткой `docker` и доступом к Docker
- креденшен `github-registry` (username + PAT с правом `write:packages`)

## Ограничения

- [ ] нет юнит-тестов на сами шаги (проверяются прогоном джобы)
- [ ] `notify` умеет только Slack
- [ ] нет `timeout` и `retry` внутри шагов — задаются в пайплайне