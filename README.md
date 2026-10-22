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
```


## Шаги

### `runTests(action: 'all', goImage: 'golang:1.25-alpine')`

Прогоняет шаги внутри контейнера с Go (`withDockerContainer`).
`action`: `install` | `lint` | `unit` | `all`. Комбинации складываются:
`all` — это `go mod download`, `go vet ./...` и `go test ./...`.
`HOME`, `GOCACHE`, `GOMODCACHE` перенаправлены в `/tmp`, иначе на агенте
ловим `permission denied` на кеше.

### `buildImage(imageName: 'app', imageTag:, dockerfile: 'Dockerfile', context: '.')`

Собирает образ и запоминает его в `env.BUILT_IMAGE`, чтобы следующий шаг
не принимал тег повторно. `imageTag` обязателен — без него шаг падает
с `imageTag is required`, дефолта нет специально: тег должен приходить
извне (в CI это первые 7 символов SHA). `imageName` по умолчанию `app`.

### `pushImage(registry: 'ghcr.io', imageName:, imageTag:, credentialsId: 'github-registry')`

Логин в registry через `--password-stdin` (пароль не попадает в лог),
`docker tag`, `docker push`, `docker logout`. Если `imageName` и `imageTag`
не переданы, берёт их из `env.BUILT_IMAGE`, то есть после `buildImage` их
можно не повторять. Если и `BUILT_IMAGE` нет — подставит `app:latest`;
это аварийный fallback, в пайплайне тег лучше передавать явно.

### `notify(status: 'unknown', message: '', channel: '#deployments', tokenCredentialId:)`

Отправляет сообщение в Slack. Токен берётся из `tokenCredentialId`, а если
он не передан — из переменной окружения `SLACK_TOKEN_CREDENTIAL_ID`.
Когда нет ни того, ни другого, шаг пишет в лог и **не валит пайплайн**:
уведомления не должны ломать сборку. Ошибки `slackSend` тоже перехватываются.

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

Шаги ломают совместимость так же, как обычный код, поэтому пайплайны
подключаются к **тегу**, а не к ветке:

```bash
git tag v1.0.0
git push origin v1.0.0
```


## Требования к Jenkins

- библиотека зарегистрирована: **Manage Jenkins → System → Global Pipeline
  Libraries** → Name `jenkins-shared-lib`, retrieval method — Git, URL
  репозитория, default version `main`, включённая опция «Allow default
  version to be overridden» (без неё `@v1.0.0` в аннотации не сработает);
- плагин **Docker Pipeline** — для `withDockerContainer`;
- плагин **Slack Notification** — для `notify`;
- агент с меткой `docker` и доступом к Docker;
- креденшел `github-registry` (username + PAT с правом `write:packages`);
- креденшел для Slack — опционально, через `tokenCredentialId` или
  `SLACK_TOKEN_CREDENTIAL_ID`.

## Ограничения

- [ ] нет юнит-тестов на сами шаги (проверяются прогоном джобы)
- [ ] `notify` умеет только Slack
- [ ] нет `timeout` и `retry` внутри шагов — задаются в пайплайне
