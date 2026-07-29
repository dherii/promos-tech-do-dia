package com.ofertas.agregador.store.amazon;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.AffiliateLinkGenerator;
import org.springframework.stereotype.Component;

/**
 * A geração de link de afiliado da Amazon NÃO depende de PA-API nem de Creators
 * API — é só anexar {@code ?tag=SEUTAG-20} na URL do produto. Isso funciona
 * independente do imbróglio de deprecação da API de dados de produto (ver
 * AmazonProductFetcher para o contexto completo).
 */
@Component
public class AmazonLinkGenerator implements AffiliateLinkGenerator {

    private final AmazonProperties properties;

    public AmazonLinkGenerator(AmazonProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateAffiliateLink(String originalProductUrl) {
        if (properties.getAssociateTag() == null || properties.getAssociateTag().isBlank()) {
            throw new IllegalStateException("amazon.associate-tag não configurado no application.properties");
        }
        char separator = originalProductUrl.contains("?") ? '&' : '?';
        return originalProductUrl + separator + "tag=" + properties.getAssociateTag();
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.AMAZON;
    }
}
