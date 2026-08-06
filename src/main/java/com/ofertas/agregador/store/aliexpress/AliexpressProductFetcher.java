package com.ofertas.agregador.store.aliexpress;

import com.ofertas.agregador.config.AliexpressProperties;
import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.ProductFetcher;
import com.ofertas.agregador.store.contract.StoreProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AliexpressProductFetcher implements ProductFetcher {

    private static final Logger log = LoggerFactory.getLogger(AliexpressProductFetcher.class);
    private final WebClient webClient;
    private final AliexpressProperties properties;

    public AliexpressProductFetcher(AliexpressProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder().baseUrl(properties.getBaseUrl()).build();
    }

    @Override
    public List<StoreProduct> fetchOffers() {
        // Prepara os parâmetros obrigatórios da API Taobao/AliExpress
        Map<String, String> params = new HashMap<>();
        params.put("method", "aliexpress.affiliate.product.query");
        params.put("app_key", properties.getAppKey());
        params.put("sign_method", "md5");
        params.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        params.put("format", "json");
        params.put("v", "2.0");

        // Parâmetros de negócio (sua curadoria)
        params.put("keywords", "hardware"); // Você pode parametrizar isso depois
        params.put("target_currency", "BRL");
        params.put("target_language", "PT");
        params.put("tracking_id", properties.getTrackingId());

        // Gera a assinatura e adiciona ao mapa
        String sign = AliexpressSignatureUtil.signRequest(params, properties.getSecret());
        params.put("sign", sign);

        try {
            AliexpressResponse response = webClient.post()
                    .uri(uriBuilder -> {
                        params.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(AliexpressResponse.class)
                    .block();

            return mapNodes(response);

        } catch (Exception ex) {
            log.error("Falha ao buscar ofertas do AliExpress", ex);
            return List.of();
        }
    }

    private List<StoreProduct> mapNodes(AliexpressResponse response) {
        List<StoreProduct> offers = new ArrayList<>();

        try {
            if (response == null || response.aliexpress_affiliate_product_query_response() == null)
                return offers;

            var result = response.aliexpress_affiliate_product_query_response().resp_result();
            if (result == null || result.resp_code() != 200 || result.result() == null)
                return offers;

            var productsList = result.result().products().product();
            if (productsList == null)
                return offers;

            for (AliexpressResponse.Product node : productsList) {
                offers.add(new StoreProduct(
                        node.product_id(),
                        node.product_title(),
                        node.promotion_link(), // Já vem com seu tracking_id
                        node.product_main_image_url(),
                        node.target_sale_price(),
                        node.target_original_price(),
                        null,
                        null,
                        null));
            }
        } catch (Exception ex) {
            log.error("Erro ao mapear produtos do AliExpress", ex);
        }

        return offers;
    }

    @Override
    public StoreType getStoreType() {
        // Exige que você adicione ALIEXPRESS no enum StoreType!
        return StoreType.ALIEXPRESS;
    }
}