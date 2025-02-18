# GitHub-AI-Bot - Coding Assistant
![GitHub AI Bot - Coding Assistant](images/github-ai-bot.png)

## What This Project Does

The GitHub AI Bot – Coding Assistant is an AI-powered code review bot for all programming language projects. It automatically analyzes pull requests (PRs) and provides feedback on code quality, best practices, and possible improvements.

### How It Works

1️⃣ Developer pushes a commit via PR → The bot is triggered via GitHub Webhook.  

2️⃣ GitHub Webhook triggers the AI Bot → Hosted on Google Cloud Platform.  

3️⃣ Bot fetches the PR code → Extracts the Java files that were modified.  

4️⃣ Bot sends the code to an AI model → Uses DeepSeek API (or another LLM) for analysis.  

5️⃣ Bot comments on the PR → Provides AI-generated code reviews with suggestions and fixes.  

### Features

✅ Automated Code Review – AI detects bad practices and suggests improvements.  

✅ Seamless GitHub Integration – Works automatically when a PR is opened.  

✅ Java Code Analysis – Specialized for all programming language projects.   

✅ Customizable AI Model – Uses DeepSeek or any other AI model for analysis.

## Java Bot

This is a Spring Boot Application.  

### Requirements
- Java 17+
- Maven 3.9+

### How to Run

1. **Build the project:**
    ```
    mvn clean install
    ```
2. **Run the application:**
    ```
    mvn spring-boot:run
    ```
3. The server should now be running on http://localhost:8080
