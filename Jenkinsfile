pipeline {
    agent {
        docker {
            image 'theboegl/jenkins-javafx-xvfb:latest'
            args '-v /root/.m2:/root/.m2'
        }
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package javadoc:jar'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
    }
	post {
        always {
            archiveArtifacts artifacts: '**/target/MCPSearch-*.jar', fingerprint: true
        }
    }
}