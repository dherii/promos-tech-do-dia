package com.ofertas.agregador.store.mercadolivre;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.ProductFetcher;
import com.ofertas.agregador.store.contract.StoreProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Busca preço/título/imagem de produtos específicos do Mercado Livre via
 * {@code GET /items/{id}}.
 *
 * DECISÃO DE DESIGN: não usamos o endpoint de busca (/sites/MLB/search) porque
 * o Mercado Livre passou a bloquear esse endpoint recentemente (múltiplos relatos
 * de 403 Forbidden mesmo com autenticação correta). Por isso este fetcher
 * ACOMPANHA uma lista de IDs configurados (mercadolivre.tracked-item-ids) em vez
 * de descobrir produtos sozinho — a descoberta de quais produtos divulgar
 * continua sendo uma decisão manual/editorial, não automatizada.
 */
@Component
public class MercadoLivreProductFetcher implements ProductFetcher {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreProductFetcher.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final MercadoLivreProperties properties;

    public MercadoLivreProductFetcher(MercadoLivreProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getApiBaseUrl())
                .build();
    }

    @Override
    public List<StoreProduct> fetchOffers() {
        List<StoreProduct> offers = new ArrayList<>();

        for (String itemId : properties.getTrackedItemIds()) {
            fetchSingleItem(itemId).ifPresent(offers::add);
        }

        return offers;
    }

    private java.util.Optional<StoreProduct> fetchSingleItem(String itemId) {
        try {
            MercadoLivreItemResponse item = webClient.get()
                    .uri("/items/{id}", itemId)
                    .headers(headers -> addAuthIfPresent(headers))
                    .retrieve()
                    .bodyToMono(MercadoLivreItemResponse.class)
                    .block(REQUEST_TIMEOUT);

            if (item == null) {
                log.warn("Resposta vazia do Mercado Livre para item {}", itemId);
                return java.util.Optional.empty();
            }

            return java.util.Optional.of(new StoreProduct(
                    item.id(),
                    item.title(),
                    item.permalink(),
                    item.thumbnail(),
                    item.price(),
                    item.original_price(),
                    item.category_id(),
                    null,  // Mercado Livre não expõe cupom no endpoint /items/{id}
                    null
            ));

        } catch (WebClientResponseException.NotFound ex) {
            log.warn("Item {} não encontrado no Mercado Livre (removido/expirado?)", itemId);
            return java.util.Optional.empty();

        } catch (WebClientResponseException.Forbidden ex) {
            log.error("403 ao buscar item {} no Mercado Livre — verifique se o token ainda é válido ou se a política da API mudou novamente", itemId);
            return java.util.Optional.empty();

        } catch (Exception ex) {
            log.error("Erro inesperado ao buscar item {} no Mercado Livre", itemId, ex);
            return java.util.Optional.empty();
        }
    }

    private void addAuthIfPresent(HttpHeaders headers) {
        if (properties.getAccessToken() != null && !properties.getAccessToken().isBlank()) {
            headers.setBearerAuth(properties.getAccessToken());
        }
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.MERCADO_LIVRE;
    }
}
