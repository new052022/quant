pipeline {
    environment {
        registry = "moritz007/quant"
        registryCredential = 'docker-hub-credentials'
    }
    tools {
        jdk 'JDK 21'
    }
    agent {
        label 'built-in'
    }
    stages {
        stage('Clean old containers') {
            steps {
                script {
                    sh '''
                    echo "Stopping and removing old containers..."
                    docker ps -a --filter ancestor=moritz007/quant --format "{{.ID}}" | xargs --no-run-if-empty docker stop || true
                    docker ps -a --filter ancestor=moritz007/quant --format "{{.ID}}" | xargs --no-run-if-empty docker rm -f || true

                    echo "Removing old images..."
                    docker images --filter reference=moritz007/quant --format "{{.ID}}" | xargs --no-run-if-empty docker rmi -f || true
                    '''
                }
            }
        }

        stage('Cloning our Git') {
            steps {
                 git branch: 'develop',
                     credentialsId: 'c1574c72-7536-44f0-b6a5-d0727c235306',
                     url: 'https://github.com/new052022/quant.git'
             }
        }

        stage('Building the application') {
            steps {
                script {
                      sh 'chmod +x gradlew && ./gradlew bootBuildImage'
                }
            }
        }

        stage('Building our image') {
            steps {
                script {
                    dockerImage = docker.build("${registry}:${BUILD_NUMBER}")
                }
            }
        }

        stage('Pushing the image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', registryCredential) {
                        dockerImage.push()
                    }
                }
            }
        }

        stage('Deploy our image') {
            steps {
                script {
                    withEnv([
                        "POSTGRES_USER=${env.POSTGRES_USER}",
                        "POSTGRES_PASS=${env.POSTGRES_PASS}",
                        "DB_HOST=${env.DB_HOST}",
                        "SECRET_NUMBER=${env.SECRET_NUMBER}",
                        "ALGORITHM=${env.ALGORITHM}",
                         "STRATEGY_URL=${env.STRATEGY_URL}",
                         "RUN_STRATEGY=${env.RUN_STRATEGY}",
                         "ORDERS_URL=${env.ORDERS_URL}",
                         "USERS_URL=${env.USERS_URL}",
                         "MARKET_URL=${env.MARKET_URL}",
                         "ASSET_DETAILS=${env.ASSET_DETAILS}"
                    ]) {
                        sh '''
                        echo "Stopping and removing previous container..."
                        docker ps -f name=quant -q | xargs --no-run-if-empty docker stop || true
                        docker ps -a -f name=quant -q | xargs --no-run-if-empty docker rm -f || true

                        echo "Deploying new container..."
                        docker run -d --name quant \
                            --network moritz-network \
                            -p 8008:8008 \
                            -e POSTGRES_USER="$POSTGRES_USER" \
                            -e POSTGRES_PASS="$POSTGRES_PASS" \
                            -e DB_HOST="$DB_HOST" \
                            -e SECRET_NUMBER="$SECRET_NUMBER" \
                            -e ALGORITHM="$ALGORITHM" \
                            -e STRATEGY_URL="$STRATEGY_URL" \
                            -e RUN_STRATEGY="$RUN_STRATEGY" \
                            -e ORDERS_URL="$ORDERS_URL" \
                            -e USERS_URL="$USERS_URL" \
                            -e MARKET_URL="$MARKET_URL" \
                            -e ASSET_DETAILS="$ASSET_DETAILS" \
                            ${registry}:${BUILD_NUMBER}
                        '''
                    }
                }
            }
        }

        stage('Cleaning up') {
            steps {
                script {
                    sh '''
                    echo "Cleaning up old images..."
                    docker images --filter reference=moritz007/quant --format "{{.ID}}" | xargs --no-run-if-empty docker rmi -f || true
                    '''
                }
            }
        }
    }
}
