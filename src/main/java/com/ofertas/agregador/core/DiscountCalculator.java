package com.ofertas.agregador.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Cálculo de desconto compartilhado entre o Scanner e o Dispatcher —
 * centralizado aqui para não duplicar a mesma regra de arredondamento em dois jobs.
 */
public final class DiscountCalculator {

    private DiscountCalculator() {
    }

    /**
     * @return o desconto percentual (0-100), ou {@code null} se não houver
     *         dados suficientes (falta preço de lista ou preço de lista <= preço atual).
     */
    public static BigDecimal percentageOff(BigDecimal listPrice, BigDecimal currentPrice) {
        if (listPrice == null || currentPrice == null || listPrice.compareTo(currentPrice) <= 0) {
            return null;
        }
        return listPrice.subtract(currentPrice)
                .divide(listPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);
    }
}
