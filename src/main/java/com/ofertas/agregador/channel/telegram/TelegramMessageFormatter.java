package com.ofertas.agregador.channel.telegram;

import com.ofertas.agregador.store.contract.StoreProduct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Responsável exclusivamente por montar o texto da mensagem em MarkdownV2.
 * Isolar essa lógica em uma classe própria (Single Responsibility) evita que
 * o {@code TelegramChannel} misture "regra de formatação" com "chamada HTTP".
 *
 * Regra de negócio chave: o código do cupom é envolvido em crase simples
 * ({@code `CODIGO`}), que no MarkdownV2 do Telegram renderiza como texto
 * monoespaçado (`code entity`) — esse é o elemento que habilita o toque
 * para copiar automaticamente no aplicativo do Telegram.
 */
@Component
public class TelegramMessageFormatter {

    /**
     * Caracteres que o MarkdownV2 do Telegram exige escapar em texto livre.
     * Referência: https://core.telegram.org/bots/api#markdownv2-style
     */
    private static final String RESERVED_CHARS = "_*[]()~`>#+-=|{}.!";

    public String format(StoreProduct product, String affiliateLink) {
        StringBuilder message = new StringBuilder();

        message.append("🔥 *").append(escape(product.title())).append("*\n\n");

        appendPriceSection(message, product);

        if (product.hasCoupon()) {
            appendCouponSection(message, product);
        }

        message.append("\n🔗 [Comprar agora](").append(escapeLinkUrl(affiliateLink)).append(")");

        return message.toString();
    }

    private void appendPriceSection(StringBuilder message, StoreProduct product) {
        BigDecimal currentPrice = product.currentPrice();
        BigDecimal listPrice = product.listPrice();

        if (listPrice != null && currentPrice != null && listPrice.compareTo(currentPrice) > 0) {
            BigDecimal discountPct = calculateDiscountPercentage(listPrice, currentPrice);
            message.append("~De R$ ").append(escape(formatMoney(listPrice))).append("~ por *R$ ")
                    .append(escape(formatMoney(currentPrice))).append("*")
                    .append(" \\(").append(escape(discountPct.toPlainString())).append("% OFF\\)\n");
        } else if (currentPrice != null) {
            message.append("💰 *R$ ").append(escape(formatMoney(currentPrice))).append("*\n");
        }
    }

    private void appendCouponSection(StringBuilder message, StoreProduct product) {
        message.append("\n🎟 Cupom: `").append(escapeCodeSpan(product.couponCode())).append("`");
        if (product.couponDescription() != null && !product.couponDescription().isBlank()) {
            message.append(" — ").append(escape(product.couponDescription()));
        }
        message.append("\n");
    }

    private BigDecimal calculateDiscountPercentage(BigDecimal listPrice, BigDecimal currentPrice) {
        BigDecimal diff = listPrice.subtract(currentPrice);
        return diff.divide(listPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);
    }

    private String formatMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    /**
     * Escapa texto livre para uso fora de entidades (negrito, tachado, link text).
     */
    private String escape(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(raw.length());
        for (char c : raw.toCharArray()) {
            if (RESERVED_CHARS.indexOf(c) >= 0) {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    /**
     * Dentro de um "code span" (crases), o Telegram só exige escapar
     * backslash e a própria crase — escapar os demais caracteres reservados
     * quebraria o código do cupom exibido ao usuário.
     */
    private String escapeCodeSpan(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("`", "\\`");
    }

    /**
     * Dentro da URL de um link `[texto](url)`, o Telegram exige escapar
     * apenas parêntese de fechamento e backslash.
     */
    private String escapeLinkUrl(String url) {
        if (url == null) {
            return "";
        }
        return url.replace("\\", "\\\\").replace(")", "\\)");
    }
}