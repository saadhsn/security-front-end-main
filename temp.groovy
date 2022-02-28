pipeline {
    agent any
    tools {
        nodejs '14.15.0'
    }

    environment {
        CI = 'false'
    }


    stages {
        stage('Repo Clone') {
            steps {
                sh "rm -rf /var/lib/jenkins/security-front-end"
                sh "mkdir /var/lib/jenkins/security-front-end"
                // Below command is used to fetch latest release build for gitlab repo
                sh 'eval $(/usr/bin/python3 /var/lib/jenkins/configs/security-front-end/fetchRelease.py)'

                dir('/var/lib/jenkins/security-front-end') {
                    
                    sh "rm -f Jenkinsfile"
                    sh "rm -f .env"
                    sh "cp /var/lib/jenkins/configs/security-front-end/" + "${params.environment}" + "_env  /var/lib/jenkins/security-front-end/.env"
                }
            }
        }
    
    
    
        stage("Install dependencies") {
            steps {
                dir('/var/lib/jenkins/security-front-end') {
                    sh 'CI=false yarn install'
                }
            }
        }

        stage("Build") {
            steps {
                dir('/var/lib/jenkins/security-front-end') {
                    sh 'CI=false yarn build'
                }
            }
        }

        stage("Deploy") {
            steps {
                dir('/var/lib/jenkins/security-front-end') {
                    sh ' pm2 delete -s front-end-'+"${params.environment}" +  " || : "
                    //sh  'pm2 serve build 3008 -spa --name front-end-'+"${params.environment}"
                    script {
                        if (params.environment == "integ" ) {
                            sh ' pm2 serve  build 3008 -spa --name front-end-'+"${params.environment}"
                        }

                        else if (params.environment == "prod") {
                            sh ' pm2 serve  build 3007 -spa --name front-end-'+"${params.environment}"
                        }

                        else if (params.environment == "stage"){
                            sh ' pm2 serve  build 3009 -spa --name front-end-'+"${params.environment}"
                        }

                    }
                }
            }
        }
    }
}
