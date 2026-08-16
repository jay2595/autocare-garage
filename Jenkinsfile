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