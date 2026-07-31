package com.ofertas.agregador.store.shopee;

// 1. IMPORT CORRIGIDO! 
import tools.jackson.databind.ObjectMapper;
import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.ProductFetcher;
import com.ofertas.agregador.store.contract.StoreProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Busca ofertas da Shopee via {@code productOfferV2} (GraphQL). Diferente do
 * Mercado Livre, a Shopee expõe busca/descoberta oficialmente — não precisamos
 * de lista de IDs rastreados aqui.
 *
 * IMPORTANTE: o {@code offerLink} retornado por este endpoint JÁ VEM com o
 * tracking de afiliado embutido. Por isso usamos ele como {@code originalUrl}
 * do StoreProduct — o ShopeeLinkGenerator correspondente é um passthrough.
 */
@Component
public class ShopeeProductFetcher implements ProductFetcher {

    private static final Logger log = LoggerFactory.getLogger(ShopeeProductFetcher.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final ShopeeProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ShopeeProductFetcher(ShopeeProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getApiBaseUrl())
                .build();
    }

    @Override
    public List<StoreProduct> fetchOffers() {
        String query = """
                query Fetch($page: Int) {
                  productOfferV2(listType: %d, sortType: %d, page: $page, limit: %d) {
                    nodes {
                      itemId
                      shopId
                      productName
                      imageUrl
                      priceMin
                      priceDiscountRate
                      productLink
                      offerLink
                    }
                  }
                }
                """.formatted(properties.getListType(), properties.getSortType(), properties.getPageLimit());

        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of("query", query, "variables", Map.of("page", 1)));
        } catch (Exception ex) {
            log.error("Falha ao serializar payload GraphQL da Shopee", ex);
            return List.of();
        }

        try {
            String authHeader = ShopeeSignatureUtil.buildAuthorizationHeader(
                    properties.getAppId(), properties.getSecret(), payload);

            // 2. CHAMADA LIMPA RESTAURADA! (O Spring converte o JSON automaticamente)
            ShopeeGraphQLResponse response = webClient.post()
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(ShopeeGraphQLResponse.class)
                    .block(REQUEST_TIMEOUT);

            if (response == null || response.data() == null || response.data().productOfferV2() == null) {
                log.warn("Resposta vazia/inesperada da Shopee productOfferV2");
                return List.of();
            }

            return mapNodes(response.data().productOfferV2().nodes());

        } catch (Exception ex) {
            log.error("Falha ao buscar ofertas da Shopee", ex);
            return List.of();
        }
    }

    private List<StoreProduct> mapNodes(List<ShopeeGraphQLResponse.Node> nodes) {
        List<StoreProduct> offers = new ArrayList<>();
        if (nodes == null) {
            return offers;
        }

        for (ShopeeGraphQLResponse.Node node : nodes) {
            try {
                offers.add(new StoreProduct(
                        node.itemId(),
                        node.productName(), // 3. NOMES DOS CAMPOS CORRIGIDOS AQUI
                        node.offerLink(), 
                        node.imageUrl(),
                        node.priceMin(),
                        deriveListPrice(node.priceMin(), node.priceDiscountRate()),
                        null, 
                        null, 
                        null
                ));
            } catch (Exception ex) {
                log.error("Falha ao mapear item {} da Shopee — item pulado", node.itemId(), ex);
            }
        }
        return offers;
    }

    /**
     * A API retorna preço atual + percentual de desconto, mas não o preço de
     * lista diretamente — derivamos aqui. Se priceDiscountRate vier nulo/zero,
     * listPrice fica igual ao currentPrice (sem desconto detectável).
     */
    private BigDecimal deriveListPrice(BigDecimal price, BigDecimal discountRate) {
        if (price == null || discountRate == null || discountRate.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }
        BigDecimal factor = BigDecimal.ONE.subtract(discountRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        if (factor.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }
        return price.divide(factor, 2, RoundingMode.HALF_UP);
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.SHOPEE;
    }
}