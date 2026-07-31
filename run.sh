#!/bin/bash

# Inicia o container do PostgreSQL se ele estiver parado
docker start postgres-ofertas

# (Opcional) Aguarda 2 segundos para o banco carregar as conexões
sleep 2

# Carrega as variáveis do arquivo .env para a memória
source .env

# Inicia o Spring Boot
./mvnw spring-boot:run