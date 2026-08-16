// AutoCare Garage - Build pipeline (CI)
//
// Clone -> build + unit test -> Sonar analysis + quality gate -> Docker images
// -> Trivy scan -> push to Azure Container Registry.
//
// The three services are built in one Maven reactor pass, then each gets its own
// image, scan and push. Every image is tagged with the Jenkins build number, which
// is what the CD pipeline consumes.

def SERVICES = ['customers-service', 'workshop-service', 'web-ui']

pipeline {

    agent any

    tools {
        jdk   'jdk21'
        maven 'maven3'
    }

    options {
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
    }

    environment {
        // Cap the JVM so Maven cannot fight SonarQube for memory on an 8GB box
        MAVEN_OPTS = '-Xmx1024m'
        // Quieter logs: batch mode, no dependency download spam
        MVN        = 'mvn -B -ntp'

        ACR_NAME   = 'acrautocarejay2595'
        ACR_LOGIN  = "${ACR_NAME}.azurecr.io"
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
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

        stage('Docker Build') {
            steps {
                script {
                    SERVICES.each { svc ->
                        // Build context is the module folder. The Dockerfile only
                        // copies the JAR Maven already produced - no second Maven
                        // run inside Docker.
                        sh "docker build -t ${ACR_LOGIN}/${svc}:${IMAGE_TAG} -t ${ACR_LOGIN}/${svc}:latest ./${svc}"
                    }
                    sh 'docker images --filter=reference="*/*:${IMAGE_TAG}" --format "{{.Repository}}:{{.Tag}}  {{.Size}}"'
                }
            }
        }

        stage('Trivy Scan') {
            steps {
                script {
                    SERVICES.each { svc ->
                        // --exit-code 0 = report but do not fail the build.
                        // Change to 1 once you have seen the report and decided
                        // which findings you are willing to block on.
                        sh """
                            trivy image \
                              --scanners vuln \
                              --severity HIGH,CRITICAL \
                              --ignore-unfixed \
                              --exit-code 0 \
                              --format table \
                              --output trivy-${svc}.txt \
                              ${ACR_LOGIN}/${svc}:${IMAGE_TAG}
                        """
                        sh "echo '----- ${svc} -----'; cat trivy-${svc}.txt"
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-*.txt', allowEmptyResults: true
                }
            }
        }

        stage('Push to ACR') {
            steps {
                withCredentials([
                    string(credentialsId: 'azure-client-id',     variable: 'AZ_CLIENT_ID'),
                    string(credentialsId: 'azure-client-secret', variable: 'AZ_CLIENT_SECRET'),
                    string(credentialsId: 'azure-tenant-id',     variable: 'AZ_TENANT_ID')
                ]) {
                    // Single quotes on purpose: Groovy must not interpolate the
                    // secrets, or they end up readable in the pipeline script.
                    sh '''
                        az login --service-principal \
                          -u "$AZ_CLIENT_ID" \
                          -p "$AZ_CLIENT_SECRET" \
                          --tenant "$AZ_TENANT_ID" \
                          --only-show-errors > /dev/null

                        az acr login --name "$ACR_NAME"
                    '''

                    script {
                        SERVICES.each { svc ->
                            sh "docker push ${ACR_LOGIN}/${svc}:${IMAGE_TAG}"
                            sh "docker push ${ACR_LOGIN}/${svc}:latest"
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo """
            ============================================================
            Build #${env.BUILD_NUMBER} succeeded.

            Images pushed to ${env.ACR_LOGIN}:
              customers-service:${env.IMAGE_TAG}
              workshop-service:${env.IMAGE_TAG}
              web-ui:${env.IMAGE_TAG}

            Feed tag ${env.IMAGE_TAG} to the CD pipeline to deploy.
            ============================================================
            """.stripIndent()
        }
        failure {
            echo "Build #${env.BUILD_NUMBER} failed. Check the red stage above."
        }
        always {
            // Log out of Azure and reclaim disk. The VM has a 30GB OS disk and
            // three Java images per build fills it faster than you would think.
            sh 'az logout --only-show-errors || true'
            sh 'docker image prune -f || true'
            cleanWs deleteDirs: true, notFailBuild: true
        }
    }
}
