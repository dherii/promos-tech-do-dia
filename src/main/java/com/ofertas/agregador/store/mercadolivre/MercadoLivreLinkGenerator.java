package com.ofertas.agregador.store.mercadolivre;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.AffiliateLinkGenerator;
import org.springframework.stereotype.Component;

/**
 * Gera o link de afiliado do Mercado Livre por manipulação de URL — não existe
 * API oficial para isso. Os parâmetros de rastreamento (matt_word/matt_tool,
 * ou affiliate=ID, dependendo da conta) devem ser copiados EXATAMENTE do link
 * gerado manualmente no Portal do Afiliado e configurados em
 * {@code mercadolivre.affiliate-query-params}.
 */
@Component
public class MercadoLivreLinkGenerator implements AffiliateLinkGenerator {

    private final MercadoLivreProperties properties;

    public MercadoLivreLinkGenerator(MercadoLivreProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateAffiliateLink(String originalProductUrl) {
        String affiliateParams = properties.getAffiliateQueryParams();

        if (affiliateParams == null || affiliateParams.isBlank()) {
            throw new IllegalStateException(
                    "mercadolivre.affiliate-query-params não configurado. " +
                    "Gere um link de exemplo no Portal do Afiliado e copie os parâmetros de query.");
        }

        char separator = originalProductUrl.contains("?") ? '&' : '?';
        return originalProductUrl + separator + affiliateParams;
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.MERCADO_LIVRE;
    }
}
