
![event-driven(5)](https://github.com/user-attachments/assets/2cc2a728-bc95-4b48-ac33-d6ceaed4e9b6)


• Java + Spring Boot rodando em EC2

• PostgreSQL no RDS para persistência de metadados

• S3 para armazenamento dos arquivos

• SQS + Lambda em Go para gerar thumbnails de forma assíncrona (evitando cold start) 

• IAM Roles (sem credenciais hardcoded no código)

• CI/CD com GitHub Actions — cada push já faz o deploy automaticamente
