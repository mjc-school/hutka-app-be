#!/bin/bash

set -eo pipefail

TEMPLATE="template.yaml"
BUILD_DIR=".aws-sam/build"
OUT_TEMPLATE="packaged-template.yaml"
STACK_NAME="hutka-stack"
BUCKET_NAME="hutka-bucket"

sam build --template "$TEMPLATE" --build-dir "$BUILD_DIR"
sam package --template-file "$BUILD_DIR/$TEMPLATE" --output-template-file "$BUILD_DIR/$OUT_TEMPLATE" --s3-bucket "$BUCKET_NAME"
sam deploy --template-file "$BUILD_DIR/$OUT_TEMPLATE" --stack-name "$STACK_NAME" --s3-bucket "$BUCKET_NAME" --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM
