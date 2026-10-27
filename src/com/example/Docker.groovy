#!/user/bin/env groovy
package com.example

class Docker implements Serializable {
    def script
    
    Docker(script) {
        this.script = script
    }

    def buildDockerImage(String imageName) {
        script.echo 'building the docker image...'
        script.sh "docker build -t $imageName ."
    }

    def dockerLogin() {
        script.withCredentials([
            script.usernamePassword(credentialsId: 'docker-hub-repo', usernameVariable: 'USER', passwordVariable: 'PASS')
        ]) {
            script.sh "echo '${script.PASS}' | docker login -u '${script.USER}' --password-stdin" 
        }
    }

    def dockerPush(String imageName) {
        script.sh "docker push $imageName"
    }

    def incrementVersion() {
        script.echo 'incrementing app version'
        script.sh 'mvn build-helper:parse-version versions:set \
            -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} versions:commit'
        def version = script.sh(
            script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout',
            returnStdout: true
        ).trim()
    
        return "${version}-${script.env.BUILD_NUMBER}"
    }

    def commitVersionUpdate() {
        script.withCredentials([
            script.usernamePassword(credentialsId: 'gh-push-creds', usernameVariable: 'USER', passwordVariable: 'PASS')
        ]) {
            script.sh 'git config user.email "jenkins@example.com"'
            script.sh 'git config user.name "Jenkins"'
            script.sh "git status"
            script.sh "git branch"
            script.sh "git config --list"
            script.sh "git remote set-url origin https://${USER}:${PASS}@github.com/osshad/java-maven-app-techwithnana.git"
            script.sh "git add ."
            script.sh 'git commit -m "ci: version bump"'
            script.sh "git push origin HEAD:jenkins-shared-lib"
        }
    }
}
