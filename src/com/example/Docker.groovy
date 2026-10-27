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
            script.gitUsernamePassword(
                credentialsId: 'gh-push-creds',
                gitToolName: 'Default'
            )
        ]) {
            script.sh '''
                git config user.email "jenkins@example.com"
                git config user.name "Jenkins"
    
                git remote set-url origin \
                    https://github.com/osshad/java-maven-app-techwithnana.git
    
                git add -- pom.xml
    
                if git diff --cached --quiet; then
                    echo "No version change to commit"
                else
                    git commit -m "ci: version bump"
                    git push origin HEAD:jenkins-shared-lib
                fi
            '''
        }
    }
}
