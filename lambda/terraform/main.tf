provider "aws" {
  access_key = var.aws_access_key
  secret_key = var.aws_secret_key
  region = var.region
}

resource "aws_dynamodb_table" "routes_table" {
  name = "Routes"
  hash_key = "id"
  billing_mode = "PROVISIONED"
  read_capacity = 5
  write_capacity = 5
  attribute {
    name = "id"
    type = "S"
  }
}


resource "aws_iam_role_policy" "write_policy" {
  name = "lambda_write_policy"
  role = aws_iam_role.write_role.id

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Action": [
        "dynamodb:BatchWriteItem",
        "dynamodb:PutItem",
        "dynamodb:UpdateItem"
      ],
      "Effect": "Allow",
      "Resource": "*"
    }
  ]
}
  EOF

}


resource "aws_iam_role_policy" "read_policy" {
  name = "lambda_read_policy"
  role = aws_iam_role.read_role.id

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "",
        "Action": [
          "dynamodb:BatchGetItem",
          "dynamodb:GetItem",
          "dynamodb:Query",
          "dynamodb:Scan"
        ],
        "Effect": "Allow",
        "Resource": "*"
      }
    ]
  }
  EOF

}


resource "aws_iam_role" "write_role" {
  name = "WriteRole"

  assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Principal": {
          "Service":
            "lambda.amazonaws.com"

        },
        "Action": "sts:AssumeRole"
      }
    ]
  }
  EOF

}


resource "aws_iam_role" "read_role" {
  name = "ReadRole"

  assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Principal": {
          "Service":
            "lambda.amazonaws.com"

        },
        "Action": "sts:AssumeRole"
      }
    ]
  }
  EOF

}


resource "aws_lambda_function" "save_route_lambda" {

  function_name = "SaveRouteLambda"
  runtime = var.lambda_runtime
  filename = var.lambda_payload_filename
  role = aws_iam_role.write_role.arn
  handler = "by.mjc.handlers.SaveRouteHandler"
  timeout = var.lambda_timeout
  memory_size = var.lambda_memory
}


resource "aws_lambda_function" "search_routes_lambda" {

  function_name = "SearchRoutesLambda"
  runtime = var.lambda_runtime
  filename = var.lambda_payload_filename
  role = aws_iam_role.read_role.arn
  handler = "by.mjc.handlers.SearchRoutesHandler"
  timeout = var.lambda_timeout
  memory_size = var.lambda_memory
}


resource "aws_api_gateway_rest_api" "api_lambda" {
  name = "hutka_app_be_api"

}


resource "aws_api_gateway_resource" "save_route_resource" {
  rest_api_id = aws_api_gateway_rest_api.api_lambda.id
  parent_id = aws_api_gateway_rest_api.api_lambda.root_resource_id
  path_part = var.save_route_path

}


resource "aws_api_gateway_method" "save_route_method" {
  rest_api_id = aws_api_gateway_rest_api.api_lambda.id
  resource_id = aws_api_gateway_resource.save_route_resource.id
  http_method = "POST"
  authorization = "NONE"
}


resource "aws_api_gateway_resource" "search_routes_resource" {
  rest_api_id = aws_api_gateway_rest_api.api_lambda.id
  parent_id = aws_api_gateway_rest_api.api_lambda.root_resource_id
  path_part = var.search_routes_path

}


resource "aws_api_gateway_method" "search_routes_method" {
  rest_api_id = aws_api_gateway_rest_api.api_lambda.id
  resource_id = aws_api_gateway_resource.search_routes_resource.id
  http_method = "POST"
  authorization = "NONE"
}


resource "aws_api_gateway_integration" "save_route_integration" {
  rest_api_id = aws_api_gateway_rest_api.api_lambda.id
  resource_id = aws_api_gateway_resource.save_route_resource.id
  http_method = aws_api_gateway_method.save_route_method.http_method

  integration_http_method = "POST"
  type = "AWS_PROXY"
  uri = aws_lambda_function.save_route_lambda.invoke_arn

}


resource "aws_api_gateway_integration" "search_routes_integration" {
  rest_api_id = aws_api_gateway_rest_api.api_lambda.id
  resource_id = aws_api_gateway_resource.search_routes_resource.id
  http_method = aws_api_gateway_method.search_routes_method.http_method

  integration_http_method = "POST"
  type = "AWS_PROXY"
  uri = aws_lambda_function.search_routes_lambda.invoke_arn

}


resource "aws_api_gateway_deployment" "api_deploy" {
  depends_on = [
    aws_api_gateway_integration.save_route_integration,
    aws_api_gateway_integration.search_routes_integration]

  rest_api_id = aws_api_gateway_rest_api.api_lambda.id
  stage_name = var.api_env_stage_name
}


resource "aws_lambda_permission" "save_route_permission" {
  statement_id = "AllowExecutionFromAPIGateway"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.save_route_lambda.function_name
  principal = "apigateway.amazonaws.com"

  source_arn = "${aws_api_gateway_rest_api.api_lambda.execution_arn}/${var.api_env_stage_name}/POST/${var.save_route_path}"

}


resource "aws_lambda_permission" "search_routes_permission" {
  statement_id = "AllowExecutionFromAPIGateway"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.search_routes_lambda.function_name
  principal = "apigateway.amazonaws.com"

  source_arn = "${aws_api_gateway_rest_api.api_lambda.execution_arn}/${var.api_env_stage_name}/POST/${var.search_routes_path}"

}


output "base_url" {
  value = aws_api_gateway_deployment.api_deploy.invoke_url
}