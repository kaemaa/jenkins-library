#!/usr/bin/env groovy

def call(Map params){
	pipeline {
		agent {
			label "${params.type}-${params.env}"
		}
		stages {
			stage('Build') {
				steps {
					sh 'mvn -B -DskipTests clean package'
				}
			}
			stage('Test') {
				steps {
					sh 'mvn test'
				}
				post {
					always {
						junit 'target/surefire-reports/*.xml'
					}
				}
			}
			stage('Build image') {
				steps {
					sh 'docker build -t my-app .'
				}
			}
			stage('Run app') {
				steps {
					sh 'docker run my-app'
				}
			}
		}
		post {
			always {
				deleteDir()
			}
		}
	}
}