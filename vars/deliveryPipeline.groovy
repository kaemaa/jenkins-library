#!/usr/bin/env groovy

def getAgent(Map params, String branch){
	echo "Deploying branch ${branch}"
	if (branch == "master"){
		echo "Deploying on agent ${params.type}-master"
		return "${params.type}-master"
	}
	echo "Deploying on agent ${params.type}-nm"
	return "${params.type}-nm"
}

def call(Map params){

	def branch = "${env.BRANCH_NAME}"

	pipeline {
		agent {
			label getAgent(params,branch)
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
			stage('Notify telegram bot') {
				steps {
					script {
						def commiter = sh(script: "git show -s --pretty=%cn", returnStdout: true).trim()
						telegramSend "${commiter} push to branch ${branch}\nBuild success."
					}
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