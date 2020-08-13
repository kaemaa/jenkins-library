#!/usr/bin/env groovy

def env = "${env.BRANCH_NAME}"

def getAgent(Map params){
	if (env == "master"){
		return "${params.type}-master"
	} else {
		return "${params.type}-nm"
	}
}

def call(Map params){
	pipeline {
		agent {
			label getAgent(params)
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