package com.ofertas.agregador.store.contract;

import com.ofertas.agregador.domain.enums.StoreType;

import java.util.List;

/**
 * Estratégia de busca de ofertas em uma loja específica.
 * A implementação concreta decide se busca via API oficial ou scraping —
 * o OfferScannerJob só conhece esta interface.
 */
public interface ProductFetcher {

    /**
     * Busca as ofertas disponíveis na loja no momento da chamada.
     * Implementações devem tratar suas próprias falhas de rede/parsing
     * e retornar lista vazia em vez de propagar exceção, para não interromper
     * a varredura das demais lojas no mesmo ciclo do scheduler.
     */
    List<StoreProduct> fetchOffers();

    /**
     * @return a loja atendida por esta implementação
     */
    StoreType getStoreType();
}
