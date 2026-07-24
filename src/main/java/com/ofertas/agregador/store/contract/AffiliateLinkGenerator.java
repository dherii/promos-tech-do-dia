package com.ofertas.agregador.store.contract;

import com.ofertas.agregador.domain.enums.StoreType;

/**
 * Estratégia de geração de link de afiliado.
 * Cada loja decide COMO transforma a URL original em link de afiliado —
 * via API oficial (ex: Amazon PA-API), manipulação de query params (Shopee, Magalu)
 * ou deep link proprietário. O motor de disparo nunca conhece esse detalhe.
 */
public interface AffiliateLinkGenerator {

    /**
     * Gera o link de afiliado a partir da URL original do produto.
     *
     * @param originalProductUrl URL original, sem tag de afiliado
     * @return URL final, pronta para ser enviada nos canais de notificação
     */
    String generateAffiliateLink(String originalProductUrl);

    /**
     * @return a loja atendida por esta implementação
     */
    StoreType getStoreType();
}
