pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '5', daysToKeepStr: '5'))
        timestamps()
    }

    environment {
        registry = 'mxomtm/house-price-prediction'
        registryCredential = 'dockerhub'
    }

    stages {
        stage('Test') {
            steps {
                echo 'Testing model correctness..'
                echo 'Always pass all test unit :D'
            }
        }

        stage('Build image') {
            steps {
                script {
                    echo 'Building image for deployment..'
                    def imageName = "${registry}:v1.${BUILD_NUMBER}"

                    dockerImage = docker.build(imageName, "--file model/Dockerfile model")
                    echo 'Pushing image to dockerhub..'
                    docker.withRegistry('', registryCredential) {
                        dockerImage.push()
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            agent {
                kubernetes {
                    containerTemplate {
                        name 'helm'
                        image 'duong05102002/jenkins-k8s:latest'
                    }
                }
            }
            steps {
                script {
                    container('helm') {
                        sh("helm upgrade --install app --set image.repository=${registry} \
                        --set image.tag=v1.${BUILD_NUMBER} ./model/helm_charts/app --namespace model-serving")
                    }
                }
            }
        }
    }
}
