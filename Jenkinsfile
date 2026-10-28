pipeline {
    agent any

    environment {
        DOCKERHUB_USER = 'ayhm00723'
        IMAGE_NAME = "${DOCKERHUB_USER}/calculator"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        // the Checkout stage does it explicitly
        skipDefaultCheckout(true)
    }

    triggers {
        pollSCM('H/2 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest .'
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_TOKEN')]) {
                    sh 'echo "$DH_TOKEN" | docker login -u "$DH_USER" --password-stdin'
                }
                sh 'docker push $IMAGE_NAME:$IMAGE_TAG'
                sh 'docker push $IMAGE_NAME:latest'
            }
            post {
                always {
                    sh 'docker logout'
                }
            }
        }

        stage('Deploy') {
            steps {
                // compose reads DOCKERHUB_USER and IMAGE_TAG from the environment block
                sh 'docker compose pull'
                sh 'docker compose up -d'
                sh '''
                    for i in $(seq 1 30); do
                        if curl -fs http://localhost:8081/actuator/health | grep -q '"status":"UP"'; then
                            echo "health check passed"
                            exit 0
                        fi
                        sleep 2
                    done
                    echo "app did not report UP within 60 seconds"
                    docker compose ps
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            emailext to: "${env.NOTIFY_EMAIL}",
                     subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: "Job: ${env.JOB_NAME}\nBuild: #${env.BUILD_NUMBER}\nStatus: SUCCESS\nImage: ${env.IMAGE_NAME}:${env.IMAGE_TAG}\nDetails: ${env.BUILD_URL}"
        }
        failure {
            emailext to: "${env.NOTIFY_EMAIL}",
                     subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: "Job: ${env.JOB_NAME}\nBuild: #${env.BUILD_NUMBER}\nStatus: FAILURE\nConsole: ${env.BUILD_URL}console"
        }
        cleanup {
            sh 'docker image prune -f'
        }
    }
}
