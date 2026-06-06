# AWS Blueprint

This folder is a takeover-ready infrastructure blueprint for the managed-service
target of RealEstate OS: Spring Boot API, Angular frontend, Aurora PostgreSQL,
Keycloak/OIDC integration, SES, S3, CloudWatch, and App Runner or ECS-style
deployment boundaries.

It is intentionally not coupled to stage secrets. Database passwords, SMTP
credentials, Keycloak client secrets, private certificates, and tokens belong in
AWS Secrets Manager or CI/CD secret storage, never in Git.

## Target Shape

- Angular as static assets behind a CDN or existing web server.
- Spring Boot API as App Runner service or ECS Fargate service.
- Aurora PostgreSQL as relational product database.
- S3 for document objects and future retention workflows.
- SES for transactional mail.
- Keycloak or compatible OIDC issuer for production identity.
- CloudWatch for logs, metrics, alarms, and operational visibility.

## Usage

```bash
terraform init
terraform plan \
  -var environment=stage \
  -var api_image=123456789012.dkr.ecr.eu-central-1.amazonaws.com/realestate-api:latest \
  -var 'vpc_id=vpc-...' \
  -var 'private_subnet_ids=["subnet-...","subnet-..."]'
```

## Before A Real Apply

Finalize these items per environment:

- VPC and subnet model
- DNS and TLS
- secrets and rotation
- database backup window and retention
- deletion protection
- SES domain verification
- document storage lifecycle
- CloudWatch alarms
- budget and cost alerts
- rollout and rollback process
