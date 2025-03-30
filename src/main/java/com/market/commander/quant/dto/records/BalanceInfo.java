package com.market.commander.quant.dto.records;

import com.market.commander.quant.dto.AccountBalanceDto;
import com.market.commander.quant.util.FinancialUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
public record BalanceInfo(BigDecimal totalBalance, BigDecimal availableBalance, BigDecimal unPnl) {

    static Optional<BalanceInfo> fromDto(AccountBalanceDto dto) {
        BigDecimal total = FinancialUtil.safeParse(dto.getBalance());
        BigDecimal available = FinancialUtil.safeParse(dto.getAvailableBalance());
        BigDecimal unPnl = FinancialUtil.safeParse(dto.getCrossUnPnl());
        if (total != null && available != null) {
            return Optional.of(new BalanceInfo(total, available, unPnl));
        }
        log.warn("Could not parse balance DTO for asset {}", dto.getAsset());
        return Optional.empty();
    }
}
