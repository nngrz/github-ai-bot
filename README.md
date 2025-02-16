# GitHub-AI-Bot - Coding Assistant
![Alt text] (images/GitHub AI Bot - Coding Assistant.png)

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
