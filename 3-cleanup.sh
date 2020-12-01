#!/bin/bash

set -eo pipefail

STACK_NAME="hutka-stack"
BUCKET_NAME="hutka-bucket"

aws cloudformation delete-stack --stack-name "$STACK_NAME"
aws s3 rm s3://"$BUCKET_NAME" --recursive
aws s3 rb s3://"$BUCKET_NAME" --force