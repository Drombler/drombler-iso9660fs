pipeline { 
    agent any  
    triggers {
        pollSCM('H/10 * * * *')
    }
    tools { 
        maven 'Apache Maven 3.6' 
        jdk 'Java SE 11' 
    }
    options {
        timestamps()
        ansiColor("xterm")
    }
    parameters {
        booleanParam(name: "RELEASE",
                description: "Build a release from current commit.",
                defaultValue: false)
    }
    stages { 
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('Build non-master branches') {
            when {
                not {
                    branch 'master'
                }
            }
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true clean install -DperformRelease=true' 
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml' 
                }
            }
        }

        stage ('Build and deploy master branch') {
            when {
                branch 'master'
            }
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true clean deploy -DperformRelease=true' 
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml' 
                }
            }
        }

        stage ('Release') {
            when {
                allOf {
                    branch 'master'
                    expression { params.RELEASE }
                }
            }
            steps {
                release {
                    sh 'mvn -Dmaven.test.failure.ignore=true clean deploy -DperformRelease=true'
                }
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml'
                }
            }
        }
    }
}