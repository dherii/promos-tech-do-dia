-- V3: Aumenta o tamanho das colunas que recebem URLs gigantes (como as do AliExpress)
ALTER TABLE product ALTER COLUMN title TYPE TEXT;
ALTER TABLE product ALTER COLUMN affiliate_url TYPE TEXT;
ALTER TABLE product ALTER COLUMN original_url TYPE TEXT;
ALTER TABLE product ALTER COLUMN image_url TYPE TEXT;