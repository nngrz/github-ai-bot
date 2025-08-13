#!/bin/bash

# 1. Extract version from pom.xml
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

# 2. Build and submit the image to Google Cloud Build
gcloud builds submit --tag gcr.io/${PROJECT_ID:?"PROJECT_ID NOT SET"}/github-ai-bot:$VERSION

# 3. Deploy the application to Cloud Run
gcloud run deploy github-ai-bot \
  --image gcr.io/${PROJECT_ID:?"PROJECT_ID NOT SET"}/github-ai-bot:$VERSION \
  --platform managed \
  --region europe-west1 \
  --allow-unauthenticated
