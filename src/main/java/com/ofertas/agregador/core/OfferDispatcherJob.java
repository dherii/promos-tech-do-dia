package com.ofertas.agregador.core;

import com.ofertas.agregador.channel.contract.NotificationChannel;
import com.ofertas.agregador.domain.ChannelConfig;
import com.ofertas.agregador.domain.DispatchLog;
import com.ofertas.agregador.domain.Product;
import com.ofertas.agregador.domain.enums.ChannelType;
import com.ofertas.agregador.domain.enums.DispatchStatus;
import com.ofertas.agregador.domain.repository.ChannelConfigRepository;
import com.ofertas.agregador.domain.repository.DispatchLogRepository;
import com.ofertas.agregador.domain.repository.ProductRepository;
import com.ofertas.agregador.store.contract.StoreProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Seleciona produtos recém-atualizados pelo OfferScannerJob, valida contra as
 * regras de cada canal ({@link ChannelConfig#accepts}) e a deduplicação
 * ({@link DispatchLogRepository#existsDuplicateDispatch}), e dispara via
 * {@link NotificationChannel}.
 *
 * Isolamento de falhas: falha ao disparar para UM destino não impede o disparo
 * para os demais destinos/canais do mesmo produto, nem para os demais produtos.
 */
@Component
public class OfferDispatcherJob {

    private static final Logger log = LoggerFactory.getLogger(OfferDispatcherJob.class);

    /**
     * Janela de segurança: cobre produtos atualizados desde bem antes do último
     * ciclo do dispatcher, para não perder itens em caso de atraso do scheduler.
     * Ajuste via `dispatcher.lookback-minutes` se o scanner rodar em intervalo maior.
     */
    private static final long LOOKBACK_MINUTES = 15;

    private final ProductRepository productRepository;
    private final ChannelConfigRepository channelConfigRepository;
    private final DispatchLogRepository dispatchLogRepository;
    private final Map<ChannelType, NotificationChannel> channels;

    public OfferDispatcherJob(List<NotificationChannel> channelList,
                               ProductRepository productRepository,
                               ChannelConfigRepository channelConfigRepository,
                               DispatchLogRepository dispatchLogRepository) {
        this.productRepository = productRepository;
        this.channelConfigRepository = channelConfigRepository;
        this.dispatchLogRepository = dispatchLogRepository;
        this.channels = channelList.stream()
                .collect(Collectors.toMap(NotificationChannel::getChannelType, c -> c));
    }

    @Scheduled(fixedDelayString = "${dispatcher.fixed-delay-ms:300000}")
    public void dispatchAll() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOOKBACK_MINUTES);
        List<Product> candidates = productRepository.findByUpdatedAtAfter(since);

        log.info("Ciclo de disparo: {} produtos candidatos (atualizados nos últimos {} min)",
                candidates.size(), LOOKBACK_MINUTES);

        for (Product product : candidates) {
            try {
                dispatchProduct(product);
            } catch (Exception ex) {
                log.error("Falha ao processar disparo do produto {} — produto pulado", product.getId(), ex);
            }
        }
    }

    private void dispatchProduct(Product product) {
        if (product.getAffiliateUrl() == null) {
            log.debug("Produto {} sem link de afiliado ainda — pulando disparo", product.getId());
            return;
        }

        BigDecimal discountPct = DiscountCalculator.percentageOff(product.getListPrice(), product.getCurrentPrice());

        for (ChannelType channelType : channels.keySet()) {
            List<ChannelConfig> configs = channelConfigRepository.findByChannelCodeAndActiveTrue(channelType);

            for (ChannelConfig config : configs) {
                try {
                    dispatchToDestination(product, discountPct, channelType, config);
                } catch (Exception ex) {
                    log.error("Falha ao disparar produto {} para destino {} ({}) — destino pulado",
                            product.getId(), config.getDestinationId(), channelType, ex);
                }
            }
        }
    }

    private void dispatchToDestination(Product product, BigDecimal discountPct,
                                        ChannelType channelType, ChannelConfig config) {
        if (!config.accepts(discountPct, product.getCategory())) {
            return;
        }

        boolean alreadySent = dispatchLogRepository.existsDuplicateDispatch(
                product.getId(), channelType, product.getCurrentPrice(), product.getCouponCode());
        if (alreadySent) {
            log.debug("Produto {} já enviado para {} nesse preço/cupom — pulando (dedup)",
                    product.getId(), config.getDestinationId());
            return;
        }

        NotificationChannel channel = channels.get(channelType);
        StoreProduct dto = toStoreProduct(product);

        DispatchStatus status = channel.send(dto, product.getAffiliateUrl(), config.getDestinationId());

        dispatchLogRepository.save(new DispatchLog(
                product, channelType, product.getCurrentPrice(), product.getCouponCode(), status));

        log.info("Disparo do produto {} para {} ({}): {}",
                product.getId(), config.getDestinationId(), channelType, status);
    }

    /**
     * Converte a entidade de volta para o DTO neutro que os canais conhecem —
     * mantém NotificationChannel desacoplado do JPA.
     */
    private StoreProduct toStoreProduct(Product product) {
        return new StoreProduct(
                product.getExternalId(),
                product.getTitle(),
                product.getOriginalUrl(),
                product.getImageUrl(),
                product.getCurrentPrice(),
                product.getListPrice(),
                product.getCategory(),
                product.getCouponCode(),
                product.getCouponDescription()
        );
    }
}
