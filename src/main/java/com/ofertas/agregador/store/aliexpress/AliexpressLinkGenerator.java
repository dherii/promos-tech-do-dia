package com.ofertas.agregador.store.aliexpress;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.AffiliateLinkGenerator;
import org.springframework.stereotype.Component;

/**
 * Passthrough — o promotion_link retornado por aliexpress.affiliate.product.query
 * já vem com o tracking_id embutido.
 */
@Component
public class AliexpressLinkGenerator implements AffiliateLinkGenerator {

    @Override
    public String generateAffiliateLink(String originalProductUrl) {
        return originalProductUrl;
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.ALIEXPRESS;
    }
}