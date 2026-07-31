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
import java.util.Optional;

/**
 * Busca preço/título/imagem de produtos específicos do Mercado Livre via
 * {@code GET /items/{id}}.
 *
 * A lista de itens rastreados e seus links de afiliado vêm do banco
 * (tabela mercadolivre_tracked_item, via MercadoLivreTrackedItemRepository) —
 * não mais do application.properties. Isso permite adicionar/remover produto
 * com um INSERT/UPDATE direto no banco, sem reiniciar a aplicação.
 */
@Component
public class MercadoLivreProductFetcher implements ProductFetcher {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreProductFetcher.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final MercadoLivreProperties properties;
    private final MercadoLivreTrackedItemRepository trackedItemRepository;

    public MercadoLivreProductFetcher(MercadoLivreProperties properties,
                                       MercadoLivreTrackedItemRepository trackedItemRepository) {
        this.properties = properties;
        this.trackedItemRepository = trackedItemRepository;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getApiBaseUrl())
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    @Override
    public List<StoreProduct> fetchOffers() {
        List<MercadoLivreTrackedItem> trackedItems = trackedItemRepository.findByActiveTrue();
        List<StoreProduct> offers = new ArrayList<>();

        for (MercadoLivreTrackedItem trackedItem : trackedItems) {
            fetchSingleItem(trackedItem).ifPresent(offers::add);
        }

        return offers;
    }

    private Optional<StoreProduct> fetchSingleItem(MercadoLivreTrackedItem trackedItem) {
        String itemId = trackedItem.getItemId();
        try {
            MercadoLivreItemResponse item = webClient.get()
                    .uri("/items/{id}", itemId)
                    .headers(this::addAuthIfPresent)
                    .retrieve()
                    .bodyToMono(MercadoLivreItemResponse.class)
                    .block(REQUEST_TIMEOUT);

            if (item == null) {
                log.warn("Resposta vazia do Mercado Livre para item {}", itemId);
                return Optional.empty();
            }

            // Usa o link de afiliado já gerado no Gerador de Produtos Recomendados
            // (guardado no banco) — não tentamos reconstruir via fórmula.
            return Optional.of(new StoreProduct(
                    item.id(),
                    item.title(),
                    trackedItem.getAffiliateUrl(),
                    item.thumbnail(),
                    item.price(),
                    item.original_price(),
                    item.category_id(),
                    null,  // Mercado Livre não expõe cupom no endpoint /items/{id}
                    null
            ));

        } catch (WebClientResponseException.NotFound ex) {
            log.warn("Item {} não encontrado no Mercado Livre (removido/expirado?)", itemId);
            return Optional.empty();

        } catch (WebClientResponseException.Forbidden ex) {
            log.error("403 ao buscar item {} no Mercado Livre — verifique se o token ainda é válido ou se a política da API mudou novamente", itemId);
            return Optional.empty();

        } catch (Exception ex) {
            log.error("Erro inesperado ao buscar item {} no Mercado Livre", itemId, ex);
            return Optional.empty();
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