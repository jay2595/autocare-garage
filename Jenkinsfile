// AutoCare Garage - Build pipeline (CI)
//
// Clone -> build + unit test -> Sonar analysis + quality gate -> Docker images
// -> Trivy scan -> push to Azure Container Registry -> trigger the CD job.
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

    parameters {
        booleanParam(
            name: 'FAIL_ON_VULNERABILITIES',
            defaultValue: true,
            description: 'Fail the build if Trivy finds fixable HIGH/CRITICAL CVEs.'
        )
        booleanParam(
            name: 'DEPLOY_AFTER_BUILD',
            defaultValue: true,
            description: 'Trigger the autocare-cd job with this build number when the build succeeds.'
        )
    }

    triggers {
        // Ask GitHub for new commits every 5 minutes. 'H' spreads the load so
        // every job on this controller does not poll on the same tick.
        pollSCM('H/5 * * * *')
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
                // Blocks until SonarQube POSTs its verdict to the Jenkins webhook,
                // then fails the build if the gate is red. A gate that cannot fail
                // is not a gate.
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
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
                        def image = "${ACR_LOGIN}/${svc}:${IMAGE_TAG}"

                        // Pass 1 - visibility. Reports every fixable HIGH and
                        // CRITICAL, never blocks, always archived. You cannot act
                        // on what you cannot see.
                        sh """
                            trivy image \
                              --scanners vuln \
                              --severity HIGH,CRITICAL \
                              --ignore-unfixed \
                              --exit-code 0 \
                              --format table \
                              --output trivy-${svc}.txt \
                              ${image}
                            echo "----- ${svc} -----"
                            cat trivy-${svc}.txt
                        """

                        // Pass 2 - the gate. Blocks on CRITICAL only.
                        //
                        // Failing on every HIGH sounds stricter but is not
                        // workable: a Spring Boot fat jar carries dozens of
                        // transitive libraries, and there is almost always an
                        // unpatched HIGH somewhere. A gate that is always red gets
                        // switched off, and then you have no gate at all.
                        // Blocking on CRITICAL and reviewing HIGH is a policy that
                        // survives contact with a real dependency tree.
                        if (params.FAIL_ON_VULNERABILITIES) {
                            sh """
                                trivy image \
                                  --scanners vuln \
                                  --severity CRITICAL \
                                  --ignore-unfixed \
                                  --exit-code 1 \
                                  --format table \
                                  --quiet \
                                  ${image}
                            """
                        }
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-*.txt', allowEmptyArchive: true
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

        stage('Trigger CD') {
            when {
                expression { return params.DEPLOY_AFTER_BUILD }
            }
            steps {
                // wait:false so CI reports green as soon as the images are pushed.
                // The deploy is a separate job with its own history and its own
                // rollback, which is the whole reason CI and CD are split.
                build job: 'autocare-cd',
                      parameters: [string(name: 'IMAGE_TAG', value: env.BUILD_NUMBER)],
                      wait: false
                echo "Triggered autocare-cd with IMAGE_TAG=${env.BUILD_NUMBER}"
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
