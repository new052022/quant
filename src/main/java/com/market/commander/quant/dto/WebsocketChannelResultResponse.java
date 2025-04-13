package com.market.commander.quant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.market.commander.quant.enums.StreamStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebsocketChannelResultResponse {

    private StreamStatusType type;

}
