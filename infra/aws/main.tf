terraform {
  required_version = ">= 1.8.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

locals {
  name = "realestate-${var.environment}"
  tags = {
    Application = "RealEstate OS"
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

resource "aws_cloudwatch_log_group" "api" {
  name              = "/realestate/${var.environment}/api"
  retention_in_days = var.log_retention_days
  tags              = local.tags
}

resource "aws_db_subnet_group" "aurora" {
  name       = "${local.name}-aurora"
  subnet_ids = var.private_subnet_ids
  tags       = local.tags
}

resource "aws_rds_cluster" "postgres" {
  cluster_identifier              = "${local.name}-postgres"
  engine                          = "aurora-postgresql"
  engine_version                  = var.aurora_postgres_version
  database_name                   = "realestate"
  master_username                 = var.db_master_username
  manage_master_user_password     = true
  db_subnet_group_name            = aws_db_subnet_group.aurora.name
  backup_retention_period         = var.backup_retention_days
  preferred_backup_window         = "02:00-03:00"
  preferred_maintenance_window    = "sun:03:00-sun:04:00"
  storage_encrypted               = true
  deletion_protection             = var.deletion_protection
  enabled_cloudwatch_logs_exports = ["postgresql"]
  skip_final_snapshot             = !var.deletion_protection
  tags                            = local.tags

  serverlessv2_scaling_configuration {
    min_capacity = var.aurora_min_capacity
    max_capacity = var.aurora_max_capacity
  }
}

resource "aws_rds_cluster_instance" "postgres" {
  count              = var.aurora_instance_count
  identifier         = "${local.name}-postgres-${count.index + 1}"
  cluster_identifier = aws_rds_cluster.postgres.id
  instance_class     = "db.serverless"
  engine             = aws_rds_cluster.postgres.engine
  engine_version     = aws_rds_cluster.postgres.engine_version
  tags               = local.tags
}

resource "aws_s3_bucket" "documents" {
  bucket = "${local.name}-documents-${var.account_suffix}"
  tags   = local.tags
}

resource "aws_s3_bucket_public_access_block" "documents" {
  bucket                  = aws_s3_bucket.documents.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "documents" {
  bucket = aws_s3_bucket.documents.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_ses_domain_identity" "mail" {
  domain = var.mail_domain
}

resource "aws_apprunner_vpc_connector" "api" {
  vpc_connector_name = "${local.name}-api"
  subnets            = var.private_subnet_ids
  security_groups    = var.api_security_group_ids
  tags               = local.tags
}

resource "aws_apprunner_service" "api" {
  service_name = "${local.name}-api"

  source_configuration {
    auto_deployments_enabled = true

    authentication_configuration {
      access_role_arn = var.ecr_access_role_arn
    }

    image_repository {
      image_identifier      = var.api_image
      image_repository_type = "ECR"

      image_configuration {
        port = "8098"

        runtime_environment_variables = {
          SPRING_DATASOURCE_URL          = "jdbc:postgresql://${aws_rds_cluster.postgres.endpoint}:5432/realestate"
          SPRING_DATASOURCE_USERNAME     = var.db_master_username
          REALESTATE_PUBLIC_BASE_URL     = var.public_base_url
          REALESTATE_IDENTITY_MODE       = "keycloak"
          REALESTATE_KEYCLOAK_ISSUER_URI = var.keycloak_issuer_uri
          REALESTATE_KEYCLOAK_CLIENT_ID  = var.keycloak_client_id
          REALESTATE_MAIL_ENABLED        = "true"
          REALESTATE_MAIL_FROM           = var.mail_from
          REALESTATE_MAIL_FROM_NAME      = "Real Estate OS"
        }
      }
    }
  }

  instance_configuration {
    cpu    = var.api_cpu
    memory = var.api_memory
  }

  network_configuration {
    egress_configuration {
      egress_type       = "VPC"
      vpc_connector_arn = aws_apprunner_vpc_connector.api.arn
    }
  }

  tags = local.tags
}
