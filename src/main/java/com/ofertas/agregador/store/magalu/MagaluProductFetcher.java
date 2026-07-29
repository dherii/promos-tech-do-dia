package com.ofertas.agregador.store.magalu;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.ProductFetcher;
import com.ofertas.agregador.store.contract.StoreProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TODO: Magalu não tem API pública de busca de produto nem de afiliados —
 * confirmei isso pesquisando antes de escrever este código. A única forma de
 * obter preço/título/imagem automaticamente é via scraping (ex: Jsoup na
 * página do produto) ou acompanhando uma lista de URLs, como fizemos com o
 * Mercado Livre.
 *
 * Não implementei o scraping aqui de propósito — HTML de e-commerce muda com
 * frequência e um scraper que eu escrever sem testar contra o site real tem
 * boa chance de quebrar silenciosamente ou (pior) ser impreciso. Prefiro te
 * entregar o contrato pronto e implementar o scraper de verdade quando você
 * tiver 2-3 URLs de produto reais em mãos para eu validar o parsing.
 *
 * Por ora, este fetcher retorna lista vazia — não quebra o OfferScannerJob,
 * só não contribui ofertas ainda.
 */
@Component
public class MagaluProductFetcher implements ProductFetcher {

    private static final Logger log = LoggerFactory.getLogger(MagaluProductFetcher.class);

    @Override
    public List<StoreProduct> fetchOffers() {
        log.debug("MagaluProductFetcher ainda não implementado (pendente de scraping) — retornando lista vazia");
        return List.of();
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.MAGALU;
    }
}
