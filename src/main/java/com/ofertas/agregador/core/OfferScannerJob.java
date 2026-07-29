package com.ofertas.agregador.core;

import com.ofertas.agregador.domain.OfferHistory;
import com.ofertas.agregador.domain.Product;
import com.ofertas.agregador.domain.Store;
import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.domain.repository.OfferHistoryRepository;
import com.ofertas.agregador.domain.repository.ProductRepository;
import com.ofertas.agregador.domain.repository.StoreRepository;
import com.ofertas.agregador.store.contract.ProductFetcher;
import com.ofertas.agregador.store.contract.StoreProduct;
import com.ofertas.agregador.store.registry.AffiliateLinkGeneratorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Varre todos os ProductFetcher ativos (um por loja, injetados automaticamente
 * pelo Spring), faz upsert em `product` e grava em `offer_history` quando
 * preço ou cupom mudam.
 *
 * Isolamento de falhas: uma loja que falhar (rede, parsing, API fora do ar)
 * NUNCA impede a varredura das demais — cada loja e cada item são isolados
 * em seu próprio try/catch.
 */
@Component
public class OfferScannerJob {

    private static final Logger log = LoggerFactory.getLogger(OfferScannerJob.class);

    private final List<ProductFetcher> fetchers;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final OfferHistoryRepository offerHistoryRepository;
    private final AffiliateLinkGeneratorRegistry linkGeneratorRegistry;

    public OfferScannerJob(List<ProductFetcher> fetchers,
                            StoreRepository storeRepository,
                            ProductRepository productRepository,
                            OfferHistoryRepository offerHistoryRepository,
                            AffiliateLinkGeneratorRegistry linkGeneratorRegistry) {
        this.fetchers = fetchers;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.offerHistoryRepository = offerHistoryRepository;
        this.linkGeneratorRegistry = linkGeneratorRegistry;
    }

    @Scheduled(fixedDelayString = "${scanner.fixed-delay-ms:900000}")
    public void scanAll() {
        log.info("Iniciando ciclo de varredura ({} lojas registradas)", fetchers.size());
        for (ProductFetcher fetcher : fetchers) {
            scanStore(fetcher);
        }
        log.info("Ciclo de varredura finalizado");
    }

    private void scanStore(ProductFetcher fetcher) {
        StoreType storeType = fetcher.getStoreType();

        Store store = storeRepository.findByCode(storeType).orElse(null);
        if (store == null) {
            log.warn("Loja {} não cadastrada na tabela `store` — pulando varredura. " +
                    "Insira uma linha em `store` com code='{}' para habilitar.", storeType, storeType);
            return;
        }
        if (!store.isActive()) {
            log.debug("Loja {} está inativa — pulando varredura", storeType);
            return;
        }

        List<StoreProduct> offers;
        try {
            offers = fetcher.fetchOffers();
        } catch (Exception ex) {
            log.error("Falha ao buscar ofertas da loja {} — varredura desta loja abortada, demais lojas seguem normalmente", storeType, ex);
            return;
        }

        log.info("Loja {}: {} ofertas retornadas pelo fetcher", storeType, offers.size());

        for (StoreProduct offer : offers) {
            try {
                upsert(store, offer);
            } catch (Exception ex) {
                log.error("Falha ao processar item {} da loja {} — item pulado", offer.externalId(), storeType, ex);
            }
        }
    }

    @Transactional
    protected void upsert(Store store, StoreProduct offer) {
        Product product = productRepository.findByStoreIdAndExternalId(store.getId(), offer.externalId())
                .orElseGet(() -> new Product(store, offer.externalId(), offer.title(), offer.originalUrl()));

        boolean isNew = product.getId() == null;
        boolean priceChanged = isNew || !equalsSafe(product.getCurrentPrice(), offer.currentPrice());
        boolean couponChanged = !equalsSafe(product.getCouponCode(), offer.couponCode());

        // Mantém o affiliateUrl anterior se a geração falhar ou não houver
        // generator registrado — evita apagar um link válido por uma falha pontual.
        String affiliateUrl = linkGeneratorRegistry.resolve(store.getCode())
                .map(generator -> {
                    try {
                        return generator.generateAffiliateLink(offer.originalUrl());
                    } catch (Exception ex) {
                        log.warn("Falha ao gerar link de afiliado para item {} da loja {}: {}",
                                offer.externalId(), store.getCode(), ex.getMessage());
                        return product.getAffiliateUrl();
                    }
                })
                .orElse(product.getAffiliateUrl());

        product.setTitle(offer.title());
        product.setImageUrl(offer.imageUrl());
        product.setCategory(offer.category());
        product.updateFromScan(offer.currentPrice(), offer.listPrice(), offer.couponCode(), offer.couponDescription(), affiliateUrl);

        productRepository.save(product);

        if (priceChanged || couponChanged) {
            BigDecimal discountPct = DiscountCalculator.percentageOff(offer.listPrice(), offer.currentPrice());
            offerHistoryRepository.save(new OfferHistory(product, offer.currentPrice(), discountPct,
                    offer.couponCode(), offer.couponDescription()));
        }
    }

    private boolean equalsSafe(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return Objects.equals(a, b);
        }
        return a.compareTo(b) == 0;
    }

    private boolean equalsSafe(String a, String b) {
        return Objects.equals(a, b);
    }
}
