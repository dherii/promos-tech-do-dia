package com.ofertas.agregador.store.mercadolivre;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.AffiliateLinkGenerator;
import org.springframework.stereotype.Component;

/**
 * Passthrough. O link de afiliado do Mercado Livre já vem pronto do
 * MercadoLivreProductFetcher (lido da tabela mercadolivre_tracked_item),
 * porque o parâmetro `ref` do link de compartilhamento é um token assinado
 * por produto que não conseguimos reconstruir por fórmula — precisa ser
 * gerado no Gerador de Produtos Recomendados e salvo no banco. Este
 * generator existe só para manter o contrato Strategy uniforme entre
 * todas as lojas (ver OfferScannerJob, que chama isso genericamente
 * pra qualquer StoreType).
 */
@Component
public class MercadoLivreLinkGenerator implements AffiliateLinkGenerator {

    @Override
    public String generateAffiliateLink(String originalProductUrl) {
        return originalProductUrl;
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.MERCADO_LIVRE;
    }
}