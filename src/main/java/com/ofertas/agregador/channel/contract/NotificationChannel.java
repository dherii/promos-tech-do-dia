package com.ofertas.agregador.channel.contract;

import com.ofertas.agregador.store.contract.StoreProduct;
import com.ofertas.agregador.domain.enums.ChannelType;
import com.ofertas.agregador.domain.enums.DispatchStatus;

/**
 * Estratégia de disparo de notificação para um canal específico (Telegram, WhatsApp, ...).
 * O {@code OfferDispatcherJob} conhece apenas este contrato — nunca a API concreta
 * de cada canal.
 */
public interface NotificationChannel {

    /**
     * Envia a oferta para um destino específico do canal (chat_id, número, etc.).
     * Implementações NÃO devem lançar exceção para falhas esperadas de rede/API;
     * devem capturá-las e retornar {@link DispatchStatus#FAILED}, para que o
     * orquestrador registre o resultado em {@code dispatch_log} sem interromper
     * o disparo para os demais destinos.
     *
     * @param product        produto normalizado, incluindo cupom (se houver)
     * @param affiliateLink  link de afiliado já gerado pelo {@code AffiliateLinkGenerator}
     * @param destinationId  identificador do destino (chat_id do Telegram, número do WhatsApp)
     * @return o status resultante do envio
     */
    DispatchStatus send(StoreProduct product, String affiliateLink, String destinationId);

    /**
     * @return o canal atendido por esta implementação
     */
    ChannelType getChannelType();
}
