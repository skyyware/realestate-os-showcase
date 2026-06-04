variable "aws_region" {
  type    = string
  default = "eu-central-1"
}

variable "environment" {
  type    = string
  default = "stage"
}

variable "account_suffix" {
  type        = string
  description = "Short unique suffix for globally named resources such as S3 buckets."
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "api_security_group_ids" {
  type    = list(string)
  default = []
}

variable "api_image" {
  type        = string
  description = "ECR image URI for the Spring Boot API."
}

variable "ecr_access_role_arn" {
  type        = string
  description = "IAM role ARN that lets App Runner pull the private ECR image."
}

variable "api_cpu" {
  type    = string
  default = "1024"
}

variable "api_memory" {
  type    = string
  default = "2048"
}

variable "public_base_url" {
  type    = string
  default = "https://realestate.stage.dev"
}

variable "db_master_username" {
  type    = string
  default = "realestate"
}

variable "aurora_postgres_version" {
  type    = string
  default = "16.6"
}

variable "aurora_min_capacity" {
  type    = number
  default = 0.5
}

variable "aurora_max_capacity" {
  type    = number
  default = 4
}

variable "aurora_instance_count" {
  type    = number
  default = 1
}

variable "backup_retention_days" {
  type    = number
  default = 7
}

variable "deletion_protection" {
  type    = bool
  default = true
}

variable "log_retention_days" {
  type    = number
  default = 30
}

variable "keycloak_issuer_uri" {
  type        = string
  description = "OIDC issuer URI, for example https://identity.example.com/realms/realestate."
}

variable "keycloak_client_id" {
  type    = string
  default = "realestate-os"
}

variable "mail_domain" {
  type    = string
  default = "realestate.stage.dev"
}

variable "mail_from" {
  type    = string
  default = "no-reply@realestate.stage.dev"
}
