variable "aws_access_key" {
  # set aws access key
  default = "Access key"
}

variable "aws_secret_key" {
  # set aws secret key
  default = "Secret key"
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

variable "search_places_path" {
  default = "search-places"
}

variable "search_routes_path" {
  default = "search-routes"
}