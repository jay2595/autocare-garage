// AutoCare Garage - Build pipeline (CI)
//
// Stage 1 of the pipeline. Clones the repo, compiles all three modules and runs
// the unit tests. Sonar, Docker, Trivy and the ACR push get added on top of this
// once this much is proven green.

pipeline {

    agent any

    tools {
        jdk   'jdk21'
        maven 'maven3'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
    }

    environment {
        // Cap the JVM so Maven cannot fight SonarQube for memory on an 8GB box
        MAVEN_OPTS = '-Xmx1024m'
        // Quieter logs: batch mode, no dependency download spam
        MVN        = 'mvn -B -ntp'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh "${MVN} clean verify"
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml',
                          allowEmptyResults: true
                }
            }
        }

        stage('Code Analysis') {
            steps {
                // 'sonarqube' is the server name from Manage Jenkins > System.
                // The wrapper injects SONAR_HOST_URL and SONAR_AUTH_TOKEN, which
                // sonar-maven-plugin picks up on its own. No secrets in this file.
                withSonarQubeEnv('sonarqube') {
                    sh "${MVN} sonar:sonar"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                // Blocks until SonarQube POSTs its verdict to the Jenkins webhook.
                // abortPipeline:false = report only. Flip to true once you have seen
                // the gate pass, so a real regression stops the build.
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Archive JARs') {
            steps {
                archiveArtifacts artifacts: '*/target/*.jar',
                                 fingerprint: true,
                                 onlyIfSuccessful: true
            }
        }
    }

    post {
        success {
            echo "Build #${env.BUILD_NUMBER} passed - three JARs produced."
        }
        failure {
            echo "Build #${env.BUILD_NUMBER} failed. Check the stage that went red."
        }
        always {
            cleanWs deleteDirs: true, notFailBuild: true
        }
    }
}
