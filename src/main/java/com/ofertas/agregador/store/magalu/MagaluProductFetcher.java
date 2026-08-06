package com.ofertas.agregador.store.magalu;

import com.microsoft.playwright.*;
import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.ProductFetcher;
import com.ofertas.agregador.store.contract.StoreProduct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class MagaluProductFetcher implements ProductFetcher {

    private static final Logger log = LoggerFactory.getLogger(MagaluProductFetcher.class);
    
    private static final String STORE_OFFERS_URL = "https://www.magazinevoce.com.br/magazinepromostechdodia/selecao/ofertasdodia/";

    @Override
    public List<StoreProduct> fetchOffers() {
        List<StoreProduct> offers = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            // MUDANÇA 1: Headless = false. O navegador vai abrir fisicamente na sua tela!
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"));
            
            Page page = context.newPage();
            log.info("Carregando loja Magalu via Playwright...");
            
            page.navigate(STORE_OFFERS_URL);
            log.info("Título da página carregada: {}", page.title());

            try {
                // MUDANÇA 2: Espera inteligente. Aguarda até 15 segundos o elemento nascer na tela.
                page.waitForSelector("a[data-testid='product-card-container']", new Page.WaitForSelectorOptions().setTimeout(15000));
            } catch (PlaywrightException e) {
                log.warn("Tempo esgotado ou bloqueio detectado! Os produtos não renderizaram. HTML parcial: {}", 
                        page.content().substring(0, Math.min(page.content().length(), 800)));
            }

            String html = page.content();
            Document doc = Jsoup.parse(html);

            for (Element card : doc.select("a[data-testid='product-card-container']")) {
                try {
                    String title = card.selectFirst("[data-testid='product-title']").text();
                    
                    String href = card.attr("href");
                    String productUrl = href.startsWith("http") ? href : "https://www.magazinevoce.com.br" + href;
                    
                    Element imgEl = card.selectFirst("img[data-testid='image']");
                    String imageUrl = imgEl != null ? imgEl.attr("src") : null;
                    
                    String rawPrice = card.selectFirst("[data-testid='price-value']").text();
                    BigDecimal currentPrice = parsePrice(rawPrice);
                    
                    Element originalPriceEl = card.selectFirst("[data-testid='price-original']");
                    BigDecimal listPrice = originalPriceEl != null ? parsePrice(originalPriceEl.text()) : currentPrice;
                    
                    String externalId = String.valueOf(productUrl.hashCode());

                    if (currentPrice != null) {
                        offers.add(new StoreProduct(
                                externalId, title, productUrl, imageUrl,
                                currentPrice, listPrice, null, null, null
                        ));
                    }
                } catch (Exception ex) {
                    log.debug("Falha ao mapear card individual da Magalu", ex);
                }
            }
        } catch (Exception ex) {
            log.error("Erro ao executar Playwright para a Magalu", ex);
        }

        log.info("Loja MAGALU: {} ofertas capturadas", offers.size());
        return offers;
    }

    private BigDecimal parsePrice(String raw) {
        if (raw == null) return null;
        String clean = raw.replaceAll("[^\\d,]", "").replace(",", ".");
        try {
            return new BigDecimal(clean);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public StoreType getStoreType() {
        return StoreType.MAGALU;
    }
}