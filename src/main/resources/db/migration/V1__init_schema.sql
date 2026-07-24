-- ============================================================
-- V1__init_schema.sql
-- Schema inicial do Agregador de Ofertas de Afiliados
-- PostgreSQL 14+
-- ============================================================

-- ------------------------------------------------------------
-- STORE: cadastro das lojas/plataformas suportadas
-- ------------------------------------------------------------
CREATE TABLE store (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(30) UNIQUE NOT NULL,      -- 'SHOPEE', 'AMAZON', 'KABUM'...
    name                VARCHAR(100) NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    integration_type    VARCHAR(20) NOT NULL,             -- 'OFFICIAL_API' | 'URL_SCRAPING'
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

COMMENT ON COLUMN store.integration_type IS 'Define se o ProductFetcher usa API oficial ou scraping/manipulação de URL';

-- ------------------------------------------------------------
-- PLATFORM_CONFIG: configuração chave-valor por loja
-- (tags de afiliado, api keys, secrets)
-- ------------------------------------------------------------
CREATE TABLE platform_config (
    id                  BIGSERIAL PRIMARY KEY,
    store_id            BIGINT NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    config_key          VARCHAR(100) NOT NULL,            -- 'AFFILIATE_TAG', 'API_KEY', 'API_SECRET'
    config_value        TEXT NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_platform_config_key UNIQUE (store_id, config_key)
);

-- ------------------------------------------------------------
-- PRODUCT: produto normalizado, independente da loja de origem
-- Já inclui coupon_code / coupon_description (cupom ATUAL vigente)
-- ------------------------------------------------------------
CREATE TABLE product (
    id                  BIGSERIAL PRIMARY KEY,
    store_id            BIGINT NOT NULL REFERENCES store(id),
    external_id         VARCHAR(150) NOT NULL,            -- SKU/ASIN/ID na loja de origem
    title               VARCHAR(500) NOT NULL,
    original_url        TEXT NOT NULL,
    affiliate_url       TEXT,
    image_url           TEXT,
    category            VARCHAR(100),
    current_price       NUMERIC(10, 2),
    list_price          NUMERIC(10, 2),                   -- preço "de"
    coupon_code         VARCHAR(50),                       -- ex: 'CUPOM50'
    coupon_description  VARCHAR(255),                      -- ex: 'Desconto extra de 10% em eletrônicos'
    last_checked_at     TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_product_store_external UNIQUE (store_id, external_id)
);

CREATE INDEX idx_product_store_price ON product (store_id, current_price);
CREATE INDEX idx_product_last_checked ON product (last_checked_at);
-- Acelera consultas do dispatcher que filtram "produtos com cupom ativo"
CREATE INDEX idx_product_coupon_active ON product (coupon_code) WHERE coupon_code IS NOT NULL;

-- ------------------------------------------------------------
-- OFFER_HISTORY: histórico de preços/cupons para auditoria e gráficos
-- ------------------------------------------------------------
CREATE TABLE offer_history (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    price               NUMERIC(10, 2) NOT NULL,
    discount_pct        NUMERIC(5, 2),
    coupon_code         VARCHAR(50),                       -- cupom vigente NO MOMENTO da captura
    coupon_description  VARCHAR(255),
    captured_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_offer_history_product_date ON offer_history (product_id, captured_at DESC);

-- ------------------------------------------------------------
-- DISPATCH_LOG: controle de envio/deduplicação
-- A constraint única evita reenviar a MESMA oferta (mesmo preço + mesmo cupom)
-- para o mesmo canal — resolve deduplicação diretamente no banco.
-- ------------------------------------------------------------
CREATE TABLE dispatch_log (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    channel_code        VARCHAR(30) NOT NULL,             -- 'TELEGRAM', 'WHATSAPP'
    price_at_dispatch   NUMERIC(10, 2) NOT NULL,
    coupon_code_at_dispatch VARCHAR(50),                   -- NULL != NULL no Postgres, ver nota abaixo
    status              VARCHAR(20) NOT NULL,              -- 'SENT' | 'FAILED' | 'SKIPPED_RATE_LIMIT'
    dispatched_at       TIMESTAMP NOT NULL DEFAULT now()
);

-- Nota importante de Postgres: UNIQUE não bloqueia duplicatas quando a coluna é NULL,
-- pois NULL nunca é igual a NULL. Por isso usamos COALESCE para normalizar
-- "sem cupom" como uma string fixa dentro do índice único.
CREATE UNIQUE INDEX uq_dispatch_dedup
    ON dispatch_log (product_id, channel_code, price_at_dispatch, COALESCE(coupon_code_at_dispatch, ''));

CREATE INDEX idx_dispatch_log_product ON dispatch_log (product_id, channel_code);

-- ------------------------------------------------------------
-- CHANNEL_CONFIG: canais/destinos de disparo (grupos Telegram, números WhatsApp)
-- ------------------------------------------------------------
CREATE TABLE channel_config (
    id                  BIGSERIAL PRIMARY KEY,
    channel_code        VARCHAR(30) NOT NULL,              -- 'TELEGRAM', 'WHATSAPP'
    destination_id      VARCHAR(150) NOT NULL,             -- chat_id do Telegram / número do WhatsApp
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    min_discount_pct    NUMERIC(5, 2) DEFAULT 0,
    category_filter     VARCHAR(100),
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_channel_destination UNIQUE (channel_code, destination_id)
);

CREATE INDEX idx_channel_config_active ON channel_config (channel_code) WHERE active = TRUE;
