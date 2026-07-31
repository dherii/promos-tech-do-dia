-- V2__mercadolivre_tracked_items.sql
-- Move a configuração de itens rastreados do Mercado Livre do application.properties
-- para o banco, permitindo adicionar/remover produtos sem reiniciar a aplicação.

CREATE TABLE mercadolivre_tracked_item (
    item_id         VARCHAR(50) PRIMARY KEY,      -- ex: 'MLB1234567890'
    affiliate_url   TEXT NOT NULL,                -- link completo gerado no Gerador de Produtos Recomendados
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_ml_tracked_item_active ON mercadolivre_tracked_item (active) WHERE active = TRUE;