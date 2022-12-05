terraform {
  backend "s3" {
    bucket = "arc-commerce-integration-terraform-state"
    key    = "terraform/arc/commerce/arc-platform-sdk"
    region = "us-east-1"
  }
}