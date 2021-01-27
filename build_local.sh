#!/bin/bash

export BUILD_TIMESTAMP=$(date -Iseconds)
export CODEBUILD_RESOLVED_SOURCE_VERSION=6e6728b3cd59e2544da955079fac64a99dd48351
export GIT_REPO=nva-search-api

sam build
sam package --template-file .aws-sam/build/template.yaml --s3-bucket publish-nva-search-api-publishartifactstorebucket-w53uphl46dbh --s3-prefix svenng --output-template-file sampackaged_raw.yaml
envsubst '${CODEBUILD_RESOLVED_SOURCE_VERSION},${GIT_REPO},${BUILD_TIMESTAMP}' < sampackaged_raw.yaml > sampackaged.yaml
sam publish  --semantic-version 0.0.8 --template sampackaged.yaml

