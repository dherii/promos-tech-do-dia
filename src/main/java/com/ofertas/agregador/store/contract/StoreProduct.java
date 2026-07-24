package com.ofertas.agregador.store.contract;

import java.math.BigDecimal;

/**
 * DTO neutro que representa um produto normalizado, independente da loja de origem.
 * Todo {@code ProductFetcher} deve retornar seus produtos nesse formato — é o contrato
 * que desacopla o motor de disparo dos detalhes de cada plataforma.
 *
 * @param externalId        identificador do produto na loja de origem (SKU, ASIN, etc.)
 * @param title              título/nome do produto
 * @param originalUrl        URL original do produto, sem tag de afiliado
 * @param imageUrl           URL da imagem principal do produto
 * @param currentPrice       preço atual (com desconto, se houver)
 * @param listPrice          preço de tabela ("de"), usado para calcular desconto percentual
 * @param category           categoria/departamento do produto, usada em filtros de canal
 * @param couponCode         código do cupom vigente, ou {@code null} se não houver cupom
 * @param couponDescription  descrição curta do cupom (ex: regra de aplicação), ou {@code null}
 */
public record StoreProduct(
        String externalId,
        String title,
        String originalUrl,
        String imageUrl,
        BigDecimal currentPrice,
        BigDecimal listPrice,
        String category,
        String couponCode,
        String couponDescription
) {

    /**
     * Indica se este produto possui um cupom ativo associado.
     * Centralizar essa checagem aqui evita espalhar {@code != null} pelo código
     * de formatação de mensagem e pelas queries do dispatcher.
     */
    public boolean hasCoupon() {
        return couponCode != null && !couponCode.isBlank();
    }
}