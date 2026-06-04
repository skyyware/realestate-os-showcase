# AWS Blueprint

Dieser Ordner ist ein uebernahmefaehiger Infrastruktur-Blueprint fuer die
Kundenvision aus der Ausschreibung: Java/Spring Boot, Angular, Aurora
PostgreSQL, Keycloak/OIDC und AWS Managed Services ohne unnoetige Komplexitaet.

Er ist bewusst nicht an Stage-Secrets gekoppelt. Passwoerter, SMTP-Zugaenge,
Keycloak-Client-Secrets und private Zertifikate gehoeren in AWS Secrets Manager
oder in die CI/CD-Secret-Verwaltung, nicht in Git.

## Zielbild

- Angular als statisches Asset hinter CDN oder bestehendem Webserver
- Spring Boot API als App Runner Service oder ECS Fargate Service
- Aurora PostgreSQL als relationale Produktdatenbank
- S3 fuer Dokumentobjekte und spaetere revisionssichere Ablage
- SES fuer Transaktionsmails
- Keycloak als OIDC-Issuer fuer produktive Identity
- CloudWatch fuer Logs, Metriken und Alarme

## Anwendung

```bash
terraform init
terraform plan \
  -var environment=stage \
  -var api_image=123456789012.dkr.ecr.eu-central-1.amazonaws.com/realestate-api:latest \
  -var 'vpc_id=vpc-...' \
  -var 'private_subnet_ids=["subnet-...","subnet-..."]'
```

Dieser Blueprint ist ein Startpunkt fuer ein internes Platform-Setup. Vor einem
echten Apply muessen Netzwerk, Domain, Secrets, Backup-Fenster, Alarme und
Kostenlimits pro Umgebung finalisiert werden.
