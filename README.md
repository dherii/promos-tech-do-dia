# 🛒 Agregador & Disparador Automatizado de Ofertas E-Commerce

> Sistema distribuído e automatizado para varredura, normalização, rastreamento de preços e disparo inteligente de ofertas de e-commerce em canais de notificação (Telegram Bot / WhatsApp).

---

## 📌 Visão Geral

O **Agregador de Ofertas** é uma solução backend desenvolvida com **Spring Boot 3** que automatiza o ciclo completo de afiliado de e-commerce:

1. **Varredura (Scanner):** Consome APIs públicas e GraphQL (Shopee, Mercado Livre, Amazon) de forma assíncrona.
2. **Rastreamento de Histórico:** Identifica variações de preço, calcula percentuais de desconto e gerencia cupons de desconto.
3. **Deduplicação & Regras:** Aplica filtros configuráveis por canal (desconto mínimo, categoria) e impede envios duplicados.
4. **Notificação (Dispatcher):** Formata dinamicamente e dispara as ofertas em MarkdownV2 para grupos do Telegram com links de afiliados gerados em tempo real.

---

## 🛠️ Stacks & Tecnologias

* **Linguagem:** Java 17+
* **Framework:** Spring Boot 3 (Spring Data JPA, Spring Scheduling)
* **Comunicação HTTP:** Spring WebClient (HTTP Reativo / Não-bloqueante)
* **Banco de Dados:** PostgreSQL (Persistência, Índices Únicos e Deduplicação)
* **APIs Externas:** Telegram Bot API (MarkdownV2), Shopee Affiliate Open API (GraphQL), Mercado Livre REST API
* **Segurança:** HMAC SHA-256 (Assinatura de requisições Shopee)
* **Build & Dev:** Maven, Docker

---

## 📐 Arquitetura e Padrões de Projeto

O sistema foi desenhado seguindo os princípios do **Clean Code** e **SOLID**:

* **Strategy Pattern:**
  * `ProductFetcher`: Abstração para varredura em diferentes plataformas (REST/GraphQL/Scraping).
  * `AffiliateLinkGenerator`: Estratégia dedicada para geração e anexação de tags de afiliados específicas por loja.
  * `NotificationChannel`: Contrato neutro para integração com diferentes canais de disparo (Telegram, WhatsApp).
* **Registry Pattern:** `AffiliateLinkGeneratorRegistry` para resolução dinâmica da estratégia de afiliados por canal.
* **Resiliência e Isolamento de Falhas:**
  * Tratamento de exceções individual por item/loja nos jobs de varredura e disparo (`OfferScannerJob` e `OfferDispatcherJob`). A falha de uma API de e-commerce ou canal de envio nunca interrompe os demais fluxos.
  * Captura graciosa de *Rate Limit* (HTTP 429) e reprocessamento controlado.
* **Deduplicação Inteligente:**
  * Validação preditiva em memória via JPA query (`COALESCE`) garantindo que a mesma oferta (mesmo preço/cupom) não seja reenviada ao mesmo grupo, combinada com constraint única (`uq_dispatch_dedup`) no PostgreSQL contra *race conditions*.

---

## 🗂️ Estrutura do Projeto

src/main/java/com/ofertas/agregador/
├── channel/              # Estratégias de canais (Telegram, WhatsApp) e formatadores
├── config/               # Configurações de propriedades e beans
├── core/                 # Jobs agendados (@Scheduled) e calculadoras de desconto
├── domain/               # Entidades JPA (Product, Store, OfferHistory, DispatchLog, etc)
│   ├── enums/            # Mapeamento de tipos (StoreType, ChannelType, DispatchStatus)
│   └── repository/       # Repositórios Spring Data JPA
└── store/                # Integrações concretas por loja (Shopee, Mercado Livre, Amazon)
    ├── contract/         # Contratos neutros (ProductFetcher, StoreProduct, LinkGenerator)
    └── registry/         # Registro dinâmico de geradores de links

---

## ⚙️ Configuração & Execução

### 1. Pré-requisitos
* Java 17+ instalado
* PostgreSQL em execução
* Credenciais de API (Telegram Bot Token, Shopee AppID/Secret, etc.)

## 2. Variáveis de Ambiente (application.properties)

### Banco de Dados
spring.datasource.url=jdbc:postgresql://localhost:5432/agregador_db
spring.datasource.username=postgres
spring.datasource.password=sua_senha

### Agendadores (Jobs)
scanner.fixed-delay-ms=900000      # 15 minutos
dispatcher.fixed-delay-ms=300000   # 5 minutos

### Telegram Bot
telegram.bot.token=${TELEGRAM_BOT_TOKEN}

### Shopee Affiliate GraphQL
shopee.app-id=${SHOPEE_APP_ID}
shopee.secret=${SHOPEE_SECRET}

## Mercado Livre API
mercadolivre.access-token=${ML_ACCESS_TOKEN}
mercadolivre.tracked-item-ids=MLB1234567,MLB9876543
mercadolivre.affiliate-query-params=matt_word=SEUCODIGO

## 3. Executando a Aplicação

### Compilar e gerar o JAR
./mvnw clean package

### Executar a aplicação
./mvnw spring-boot:run

---

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.
