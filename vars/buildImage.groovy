#!/user/bin/env groovy

def call() {
    echo 'building the docker image...'
    withCredentials([
        usernamePassword(credentialsId: 'docker-hub-repo', usernameVariable: 'USER', passwordVariable: 'PASS')
    ]) {
        sh 'docker build -t olushad/demo-app:jma-2.0 .'
        sh 'echo $PASS | docker login -u $USER --password-stdin'
        sh 'docker push olushad/demo-app:jma-2.0'
    }
}
