variable "aws_access_key" {
  # set aws access key
  default = "AKIASBLUHOGNEK5NWFLC"
}

variable "aws_secret_key" {
  # set aws secret key
  default = "73ISy0/pCyqrHXao790LJzX2WBye4YYVlWJAMBOz"
}

variable "region" {
  # set aws region
  default = "eu-central-1"
}

variable "lambda_payload_filename" {
  default = "build/libs/hutka-app-be.jar"
}

variable "lambda_runtime" {
  default = "java11"
}

variable "lambda_timeout" {
  default = "60"
}

variable "lambda_memory" {
  default = "512"
}

variable "api_env_stage_name" {
  default = "Prod"
}

variable "save_route_path" {
  default = "save-route"
}

variable "search_routes_path" {
  default = "search-routes"
}