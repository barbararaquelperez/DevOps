def account           = '743085419011'
def cluster_name      = 'android'
def service_name      = "movies"
def branch            = 'main'
def region            = 'eu-west-1'
def role              = 'JenkinsRole'

def github_repository = 'https://github.com/jjhernandez-usj/movies-service'
def git_credentials   = 'github-multibranch'

if(destination_environment == 'prod' ) {
        branch = 'main'
}

cluster = "${cluster_name}-${destination_environment}"
namespace = "${cluster}"
service = "${service_name}-${destination_environment}-service"
docker_image_name = "${service_name}"
task = "${service_name}-${destination_environment}-task"

def ecr_registry_url  = "${account}.dkr.ecr.${region}.amazonaws.com"

pipeline {

    agent any

    parameters {
            string(name: 'destination_environment', defaultValue: 'prod', description: 'Destination environment')
    }
    stages {
        stage('Clone repository') {
            steps {
                script {
                    checkout([$class: 'GitSCM', branches: [[name: "*/${branch}"]],
                    doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [],
                    userRemoteConfigs: [[credentialsId: "${git_credentials}",
                    url: "${github_repository}"]]])
                }
            }
        }
        stage('Compile gradle') {
            steps {
                script {
                    sh 'chmod +x gradlew'
                    sh "echo Building project."
                    sh "./gradlew clean bootJar -Pprofile=${destination_environment}"
                }
            }
        }
        stage('Build, tag and push docker image to AWS') {
            steps {
                script {
                    sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecr_registry_url}"
                    sh "aws ecr describe-repositories --region ${region} --repository-names ${namespace}/${docker_image_name} || aws ecr create-repository --region ${region} --repository-name ${namespace}/${docker_image_name}"
                    docker.withRegistry("https://${ecr_registry_url}/") {
                        def customImage = docker.build("${namespace}/${docker_image_name}:${env.BUILD_ID}")
                        customImage.tag(env.BUILD_ID)
                        customImage.push()
                        customImage.push("latest")
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    sh "aws ecs update-service --force-new-deployment --cluster ${cluster} --region ${region} --service ${service} --task-definition ${task} --desired-count 1"
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
