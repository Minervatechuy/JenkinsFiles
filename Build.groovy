pipeline{
    agent {
        label 'vm_host'
    }
    environment {
       REPOURL = "https://github.com/Minervatechuy/Admin-API.git"
       BRANCH = "main"
       DOCKER_TAG = "1"
    }

    stages{
        stage('Checkout'){
            steps{
                checkout([$class: 'GitSCM', branches: [[name: BRANCH]], extensions: [], userRemoteConfigs: [[url: REPOURL]]])
            }
        }
        stage('Build docker image'){
            agent {
                label 'vm_host'
            }
            steps{
                sh '''
                    #docker build -t admin-api:${DOCKER_TAG} .
                    #docker tag admin-api:${DOCKER_TAG} admin-api:latest
                    cd ..
                    cd api
                    #echo ${WORKSPACE}
                    docker-compose up -d --build
                '''
            }
        }
        stage('Docker login and push'){
            agent {
                label 'vm_host'
            }
            steps{
                sh '''
                    #docker login --username fabianl1980 --password Lautaro3101
                    cat ${WORKSPACE}/Docker_password.txt | docker login --username fabianl1980 --password-stdin 
                    docker tag fabianl1980/web-flask-server:latest fabianl1980/web-flask-server:${env.BUILD_NUMBER}
                    docker tag fabianl1980/web-nginx-php:latest fabianl1980/web-nginx-php:${env.BUILD_NUMBER}
                    docker push fabianl1980/web-flask-server:${env.BUILD_NUMBER}
                    docker push fabianl1980/web-flask-server:latest
                    docker push fabianl1980/web-nginx-php:latest
                    docker push fabianl1980/web-nginx-php:${env.BUILD_NUMBER}
                '''
            }
        } //fin stage upload
        
        
        stage("Post") {
            agent {
                label 'vm_host'
            }
            steps {
                sh '''
                    pwd
                    echo "Clean up workfolder"
                    #rm -Rf *
                '''
            }
        } //fin stage post
        
    }
}