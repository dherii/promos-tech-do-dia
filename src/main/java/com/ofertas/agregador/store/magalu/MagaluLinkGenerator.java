package com.ofertas.agregador.store.magalu;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.AffiliateLinkGenerator;
import org.springframework.stereotype.Component;

/**
 * Gera o link de afiliado do Magalu anexando partner_id + promoter_id —
 * confirmados como fixos por conta ao inspecionar um link real gerado no
 * Portal Parceiro Magalu (diferente do Mercado Livre, aqui NÃO muda por produto).
 */
@Component
public class MagaluLinkGenerator implements AffiliateLinkGenerator {

    private final MagaluProperties properties;

    public MagaluLinkGenerator(MagaluProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateAffiliateLink(String originalProductUrl) {
        if (isBlank(properties.getPartnerId()) || isBlank(properties.getPromoterId())) {
            throw new IllegalStateException(
                    "magalu.partner-id / magalu.promoter-id não configurados. " +
                    "Pegue esses valores de um link real gerado no Portal Parceiro Magalu.");
        }
        char separator = originalProductUrl.contains("?") ? '&' : '?';
        return originalProductUrl + separator + "partner_id=" + properties.getPartnerId()
                + "&promoter_id=" + properties.getPromoterId();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.MAGALU;
    }
}