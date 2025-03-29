package com.market.commander.quant.dto;

import com.market.commander.quant.entities.OpenPosition;
import com.market.commander.quant.entities.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenPositionDto {

    private Order order;

    private OpenPosition position;

}
