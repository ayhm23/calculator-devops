# Calculator DevOps

A Spring Boot calculator REST API with a CI/CD pipeline using GitHub, Jenkins, Docker and Docker Compose. A push to `main` builds, tests, packages and containerizes the app, pushes the image to Docker Hub and redeploys the stack, with no manual steps.

- Docker Hub image: https://hub.docker.com/r/ayhm00723/calculator
- Stack: Java 17, Spring Boot 3.5, Maven (wrapper), JUnit 5, Docker, Docker Compose, Nginx, Jenkins

## Pipeline

```
git push (main)
     |
     v
GitHub --webhook (ngrok) / Poll SCM every 2 min--> Jenkins
                                                      |
   Checkout -> Build -> Test -> Docker Build -> Push -> Deploy -> email
                         |                       |        |
                   JUnit report          Docker Hub   docker compose up -d
                                    ayhm00723/calculator:<build>
                                                          |
                                                          v
                                       nginx :8081 -> calculator :8080
```

| Stage | What it does |
|---|---|
| Checkout | Checks out the commit that triggered the build |
| Build | `./mvnw clean package -DskipTests` |
| Test | `./mvnw test`, publishes the JUnit XML reports; a failing test stops the pipeline |
| Docker Build | Builds the image, tagged with the build number and `latest` |
| Push | Logs in to Docker Hub with Jenkins credentials and pushes both tags |
| Deploy | `docker compose pull` and `docker compose up -d`, then waits up to 60 s for `/actuator/health` to report `UP` through Nginx |

After every run Jenkins emails the result (success or failure) and removes dangling images. The pipeline is defined in the [Jenkinsfile](Jenkinsfile).

## API

All endpoints are `GET` and take two numbers `a` and `b`.

| Operation | Endpoint |
|---|---|
| Add | `/api/v1/add?a=2&b=3` |
| Subtract | `/api/v1/subtract?a=10&b=4` |
| Multiply | `/api/v1/multiply?a=6&b=7` |
| Divide | `/api/v1/divide?a=7&b=2` |
| Health | `/actuator/health` |
| Version | `/actuator/info` |

Success:
```
$ curl "localhost:8081/api/v1/add?a=2&b=3"
{"operation":"add","a":2.0,"b":3.0,"result":5.0}
```

Errors return HTTP 400 with a JSON body:
```
$ curl "localhost:8081/api/v1/divide?a=1&b=0"
{"status":400,"error":"Bad Request","message":"Division by zero is not allowed","path":"/api/v1/divide","timestamp":"..."}

$ curl "localhost:8081/api/v1/add?a=2"
{"status":400,"error":"Bad Request","message":"Required parameter 'b' is missing",...}

$ curl "localhost:8081/api/v1/add?a=abc&b=3"
{"status":400,"error":"Bad Request","message":"Parameter 'a' must be a number, got 'abc'",...}
```

Health and version:
```
$ curl localhost:8081/actuator/health
{"status":"UP",...}

$ curl localhost:8081/actuator/info
{"app":{"name":"calculator","version":"1.0.0"}}
```
The version comes from `pom.xml`.

## Build and test

Needs JDK 17. Maven is not required, the wrapper downloads it.

```
./mvnw clean package     # builds target/calculator.jar and runs the tests
./mvnw test              # tests only; reports in target/surefire-reports/
```

There are 16 unit tests for the service and 9 MockMvc integration tests for the endpoints, including the error cases.

Run the jar directly (port 8080 is taken by Jenkins on my machine, so use another one):
```
java -jar target/calculator.jar --server.port=9090
curl "localhost:9090/api/v1/add?a=2&b=3"
```

## Run with Docker Compose

The stack has two services on a private network:
- `calculator`: the app, not published on the host, with a health check on `/actuator/health`
- `nginx`: reverse proxy on host port 8081, forwards `/api/`, `/actuator/health` and `/actuator/info`

```
cp .env.example .env              # set DOCKERHUB_USER=ayhm00723
docker compose up -d              # pulls the image from Docker Hub
docker compose ps                 # both should be (healthy)
curl "localhost:8081/api/v1/add?a=2&b=3"
docker compose down
```

To build the image locally instead of pulling it: `docker compose up -d --build`.

The compose project name is fixed (`calculator`), so a local run and Jenkins manage the same stack.

## Docker image

Multi-stage [Dockerfile](Dockerfile): a Maven + JDK 17 stage builds the jar, and a slim JRE 17 Alpine stage runs it as a non-root user. `.dockerignore` only lets `pom.xml`, `src` and the wrapper into the build context.

```
docker build -t ayhm00723/calculator:dev .
docker pull ayhm00723/calculator:latest
```

## Jenkins setup

Jenkins runs directly on the host (WSL Ubuntu), so it uses the same Docker daemon as the deployment. Required:

- Plugins: Pipeline, Git, GitHub, Docker Pipeline, JUnit, Email Extension, Credentials Binding, Timestamper
- The `jenkins` user in the `docker` group
- Credentials (Manage Jenkins > Credentials):
  - `dockerhub-creds`: Docker Hub username and access token
  - `gmail-smtp`: Gmail address and App Password, used by Extended E-mail Notification (smtp.gmail.com, port 465, SSL)
- Global environment variable `NOTIFY_EMAIL`: where build emails go
- Job `calculator-pipeline`: Pipeline script from SCM, this repository, branch `*/main`, script path `Jenkinsfile`

### Triggers

- Poll SCM every 2 minutes. Works without any setup because Jenkins contacts GitHub.
- GitHub webhook for instant builds. Jenkins runs on a laptop with no public address, so the webhook goes through an ngrok tunnel: `ngrok http 8080`, and on GitHub (Settings > Webhooks) the payload URL `https://<ngrok-url>/github-webhook/` with content type `application/json` and push events only.

Both are declared in the Jenkinsfile (`githubPush()` and `pollSCM`). When the tunnel is down, polling still picks up the push.

## Secrets

No credentials are stored in this repository. The Docker Hub token and the Gmail App Password are in the Jenkins credentials store and are masked in build logs; the notification address is a Jenkins variable. `.env` is gitignored.

## Repository structure

```
calculator-devops/
  ├── src/main/java/com/spe/calculator/
  │     ├── CalculatorApplication.java
  │     ├── controller/CalculatorController.java
  │     ├── service/CalculatorService.java
  │     ├── model/            CalculationResponse, ErrorResponse
  │     └── exception/        DivisionByZeroException, GlobalExceptionHandler
  ├── src/main/resources/application.properties
  ├── src/test/java/com/spe/calculator/
  │     ├── service/CalculatorServiceTest.java
  │     └── controller/CalculatorControllerIntegrationTest.java
  ├── pom.xml
  ├── mvnw, mvnw.cmd, .mvn/
  ├── Dockerfile
  ├── .dockerignore
  ├── docker-compose.yml
  ├── nginx/nginx.conf
  ├── .env.example
  ├── Jenkinsfile
  ├── .gitignore
  └── README.md
```

## Branching

Features were developed on branches (`feature/calculator-api`, `feature/docker`) and merged into `main` through pull requests. `main` is what Jenkins builds and deploys.
