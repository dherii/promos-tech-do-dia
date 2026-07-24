package com.ofertas.agregador.channel.telegram;

import com.ofertas.agregador.config.TelegramProperties;
import com.ofertas.agregador.channel.NotificationChannel;
import com.ofertas.agregador.store.contract.StoreProduct;
import com.ofertas.agregador.domain.enums.ChannelType;
import com.ofertas.agregador.domain.enums.DispatchStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


import java.time.Duration;
import java.util.Map;

/**
 * Implementação de {@link NotificationChannel} para o Telegram, usando a API oficial
 * de bots via WebClient (não-bloqueante).
 *
 * Ponto de partida do disparo real do sistema: recebe o produto já normalizado
 * e o link de afiliado já gerado, monta a mensagem em MarkdownV2 e chama
 * {@code POST /bot<token>/sendMessage}.
 */
@Component
public class TelegramChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(TelegramChannel.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final TelegramMessageFormatter messageFormatter;

    public TelegramChannel(TelegramProperties properties, TelegramMessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getApiBaseUrl() + "/bot" + properties.getBotToken())
                .build();
    }

    @Override
    public DispatchStatus send(StoreProduct product, String affiliateLink, String destinationId) {
        String text = messageFormatter.format(product, affiliateLink);

        try {
            webClient.post()
                    .uri("/sendMessage")
                    .bodyValue(Map.of(
                            "chat_id", destinationId,
                            "text", text,
                            "parse_mode", "MarkdownV2",
                            "disable_web_page_preview", false
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block(REQUEST_TIMEOUT);

            return DispatchStatus.SENT;

        } catch (WebClientResponseException.TooManyRequests ex) {
            // Telegram retorna 429 com "retry_after" (em segundos) no corpo da resposta.
            // O ChannelRateLimiter/orquestrador deve ler esse valor para reagendar o envio;
            // aqui apenas sinalizamos o status para não travar o restante do lote.
            log.warn("Rate limit do Telegram atingido para destino {}. Corpo: {}",
                    destinationId, ex.getResponseBodyAsString());
            return DispatchStatus.SKIPPED_RATE_LIMIT;

        } catch (WebClientResponseException ex) {
            log.error("Falha ao enviar mensagem ao Telegram para destino {}. Status={} Corpo={}",
                    destinationId, ex.getStatusCode(), ex.getResponseBodyAsString());
            return DispatchStatus.FAILED;

        } catch (Exception ex) {
            log.error("Erro inesperado ao enviar mensagem ao Telegram para destino {}", destinationId, ex);
            return DispatchStatus.FAILED;
        }
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.TELEGRAM;
    }
}