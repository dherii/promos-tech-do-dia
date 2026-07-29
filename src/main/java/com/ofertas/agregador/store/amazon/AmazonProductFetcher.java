package com.ofertas.agregador.store.amazon;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.ProductFetcher;
import com.ofertas.agregador.store.contract.StoreProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ATENÇÃO — leia antes de habilitar:
 *
 * A PA-API 5.0 (a API "clássica" de dados de produto da Amazon, com assinatura
 * AWS Signature v4) foi APOSENTADA em 15/05/2026. A sucessora é a Creators API
 * (OAuth2 + Bearer token, host creatorsapi.amazon), mas ela exige do afiliado
 * um mínimo de 10 vendas qualificadas nos últimos 30 dias para liberar acesso.
 *
 * Ou seja: como afiliado novo, você provavelmente NÃO vai conseguir usar esta
 * classe ainda — a Amazon simplesmente não libera a credencial. Por isso ela
 * fica DESABILITADA por padrão (amazon.creators-api-enabled=false) e não é
 * sequer registrada como bean nesse estado, então não atrapalha o restante do
 * OfferScannerJob.
 *
 * Quando (e se) você atingir a elegibilidade, esta classe precisa de:
 *   1. Fluxo OAuth2 client_credentials contra o endpoint de token da Amazon
 *      para obter um Bearer token (cache em memória até expirar).
 *   2. POST para {host}/catalog/v1/getItems com os ASINs e o header
 *      x-marketplace identificando o marketplace (BR).
 * Os detalhes exatos de payload/headers da Creators API ainda são recentes
 * (a API mudou em 2026) — valide contra a documentação oficial atual antes
 * de confiar neste esqueleto em produção.
 */
@Component
@ConditionalOnProperty(prefix = "amazon", name = "creators-api-enabled", havingValue = "true")
public class AmazonProductFetcher implements ProductFetcher {

    private static final Logger log = LoggerFactory.getLogger(AmazonProductFetcher.class);

    private final AmazonProperties properties;

    public AmazonProductFetcher(AmazonProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<StoreProduct> fetchOffers() {
        log.warn("AmazonProductFetcher habilitado, mas a implementação contra a Creators API " +
                "ainda não foi validada em produção — retornando lista vazia. " +
                "Verifique elegibilidade (10 vendas/30 dias) e a documentação atual antes de implementar.");
        return List.of();
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.AMAZON;
    }
}
