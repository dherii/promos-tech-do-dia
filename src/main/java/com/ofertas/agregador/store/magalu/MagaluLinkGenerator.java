package com.ofertas.agregador.store.magalu;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.AffiliateLinkGenerator;
import org.springframework.stereotype.Component;

/**
 * Gera o link de afiliado do Magalu por manipulação de URL — confirmei que
 * não existe API oficial para isso (mesma situação do Mercado Livre). Os
 * parâmetros devem ser copiados de um link real gerado no Portal Parceiro
 * Magalu (parceiro.magazineluiza.com.br).
 */
@Component
public class MagaluLinkGenerator implements AffiliateLinkGenerator {

    private final MagaluProperties properties;

    public MagaluLinkGenerator(MagaluProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateAffiliateLink(String originalProductUrl) {
        String params = properties.getAffiliateQueryParams();

        if (params == null || params.isBlank()) {
            throw new IllegalStateException(
                    "magalu.affiliate-query-params não configurado. " +
                    "Gere um link de exemplo no Portal Parceiro Magalu e copie os parâmetros de query.");
        }

        char separator = originalProductUrl.contains("?") ? '&' : '?';
        return originalProductUrl + separator + params;
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.MAGALU;
    }
}
