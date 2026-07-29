package com.ofertas.agregador.store.shopee;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.AffiliateLinkGenerator;
import org.springframework.stereotype.Component;

/**
 * Para a Shopee, o {@code offerLink} retornado por {@code productOfferV2} JÁ VEM
 * com o tracking de afiliado embutido (ver ShopeeProductFetcher). Por isso este
 * generator é um passthrough — existe apenas para manter o contrato Strategy
 * uniforme entre todas as lojas, não porque haja transformação real a fazer aqui.
 */
@Component
public class ShopeeLinkGenerator implements AffiliateLinkGenerator {

    @Override
    public String generateAffiliateLink(String originalProductUrl) {
        return originalProductUrl;
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.SHOPEE;
    }
}
