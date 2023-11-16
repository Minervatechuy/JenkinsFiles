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
                script {
                    def build_version="${env.BUILD_NUMBER}"
                    env.VERSION=build_version
                    sh '''
                        #docker login --username fabianl1980 --password Lautaro3101
                        cat ${WORKSPACE}/Docker_password.txt | docker login --username fabianl1980 --password-stdin 
                        docker tag fabianl1980/web-flask-server:latest fabianl1980/web-flask-server:${VERSION}
                        docker tag fabianl1980/web-nginx-php:latest fabianl1980/web-nginx-php:${VERSION}
                        docker push fabianl1980/web-flask-server:${VERSION}
                        docker push fabianl1980/web-flask-server:latest
                        docker push fabianl1980/web-nginx-php:latest
                        docker push fabianl1980/web-nginx-php:${VERSION}
                    '''
                }
            }
        } //fin stage upload


        stage("API-Test-Automation") {
            agent {
                label 'vm_host'
            }
            steps {
                sh '''
                    export API_getCalcsInfo=$(curl -s -X POST -o /dev/null -w "%{http_code}" --location 'http://172.31.35.158:5000/getCalcsInfo' --header 'Content-Type: application/json' --data-raw '{  "user": "gabriela.perez@estudiantes.utec.edu.uy",  "pwd": "123"}')
                    echo $API_getCalcsInfo
                '''
            }
            steps {
                sh '''
                    export API_get_usuario=$(curl -s -X POST -o /dev/null -w "%{http_code}" --location 'http://172.31.35.158:5000/get_usuario' --header 'Content-Type: application/json' --data-raw '{  "usuario": "gabriela.perez@estudiantes.utec.edu.uy"}')
                    echo $API_get_usuario
                '''
            }
        } //fin stage post

         
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