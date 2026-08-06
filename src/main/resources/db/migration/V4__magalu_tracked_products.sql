-- V4__magalu_tracked_products.sql
-- Lista de produtos do Magalu rastreados via scraping (não há API pública).
-- O link de afiliado é reconstruído por fórmula (partner_id + promoter_id fixos
-- da conta), então aqui só precisamos guardar a URL canônica do produto.

CREATE TABLE magalu_tracked_product (
    id              BIGSERIAL PRIMARY KEY,
    product_url     TEXT NOT NULL UNIQUE,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_magalu_tracked_active ON magalu_tracked_product (active) WHERE active = TRUE;