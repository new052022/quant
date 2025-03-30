package com.market.commander.quant.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "open_positions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class OpenPosition extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_price")
    private Double entryPrice;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "entry_date")
    private LocalDateTime entryDate;

}
