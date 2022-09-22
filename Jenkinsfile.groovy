pipeline {
  agent any

  environment {
    APP_NAME = "dexter"
  }

  stages {
    stage('Code Repository'){
      steps {
        git url: 'https://github.com/4linux/Dexter-Intranet'
        stash 'dexter-repositorio'
      }
    }

    stage('Pre-Build - Sonarqube'){
      environment {
        SCANNER = tool 'sonar-scanner'
      }
      steps {
        withSonarQubeEnv('sonarqube'){
          sh "${SCANNER}/bin/sonar-scanner -Dsonar.projectKey=${APP_NAME} -Dsonar.sources=${WORKSPACE}/intranet/ -Dsonar.projectVersion=${BUILD_NUMBER}"
        }
        waitForQualityGate abortPipeline: true
      }
    }

    stage('Artifact Repository - clair'){
      agent { node 'automation' }
      steps {
        unstash 'dexter-repositorio'
        script {
          docker.build "${APP_NAME}:${BUILD_NUMBER}"
          sh "claircli -l 172.17.0.1 -T Medium ${APP_NAME}:${BUILD_NUMBER}"
        }
      }  
    }
  }

  post {
    always { chuckNorris() }
    success { echo "Show" }
    failure { echo "Deu Ruim.." }
  }
}