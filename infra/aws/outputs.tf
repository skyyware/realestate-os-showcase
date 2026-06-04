output "api_url" {
  value = aws_apprunner_service.api.service_url
}

output "aurora_endpoint" {
  value = aws_rds_cluster.postgres.endpoint
}

output "documents_bucket" {
  value = aws_s3_bucket.documents.bucket
}
