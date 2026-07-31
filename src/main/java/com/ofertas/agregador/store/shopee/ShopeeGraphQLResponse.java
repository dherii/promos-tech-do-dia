package com.ofertas.agregador.store.shopee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapeia a resposta de {@code productOfferV2}. Alguns campos aqui (imageUrl,
 * discountPct) NÃO foram 100% confirmados contra a documentação oficial ao
 * escrever este código — se o GraphQL rejeitar algum campo com erro de schema,
 * remova-o da query em {@link ShopeeProductFetcher} e ajuste este record.
 * Recomendo rodar uma introspection query contra o endpoint antes de ir pra produção.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record ShopeeGraphQLResponse(Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Data(ProductOfferV2 productOfferV2) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProductOfferV2(List<Node> nodes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Node(
            String itemId,
            Long shopId,
            String productName,      // Era 'name'
            String imageUrl,
            BigDecimal priceMin,     // Era 'price'
            BigDecimal priceDiscountRate, // Era 'discountPct'
            String productLink,
            String offerLink
    ) {}
}
