package com.market.commander.quant.dto;

import com.market.commander.quant.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunSessionResponseDto {

    private SessionStatus status;

}
