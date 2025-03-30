package com.market.commander.quant.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class FinancialUtil {

    public static BigDecimal safeParse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.error("Failed to parse BigDecimal from string: '{}'", value); // Avoid logging exception trace here unless needed
            return null;
        }
    }

}
