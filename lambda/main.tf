locals {
  stack = "terraform-hutka-app-be"
  name = "terraform-hutka-app-be"
}

module "hutka-app-be" {
  source = "./terraform/"
}