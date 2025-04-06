package com.market.commander.quant.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class RoundNumbers {

    public static Double toOpenPriceByAssetParam(Double tickSize, Double price) {
        log.info("Tick size: {}; price: {}", tickSize, price);
        if (tickSize == null || tickSize <= 0 || price == null) {
            return null;
        }
        int scale = (int) Math.abs(Math.log10(tickSize));
        double roundedPrice = Math.round(price / tickSize) * tickSize;
        BigDecimal bd = new BigDecimal(roundedPrice).setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static BigDecimal toAssetSize(Double marketLotSize, BigDecimal assetSize) {
        log.info("Market lot size: {}; asset size: {}", marketLotSize, assetSize);
        if (marketLotSize == null || marketLotSize <= 0 || assetSize == null) {
            return assetSize.setScale(0, RoundingMode.HALF_UP);
        }
        int scale = (int) Math.abs(Math.log10(marketLotSize));
        return assetSize.divide(BigDecimal.valueOf(marketLotSize), 0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(marketLotSize))
                .setScale(scale, RoundingMode.HALF_UP);
    }
}
