package com.banknifty.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "instrument_master",
        indexes = {
                @Index(name = "idx_exchange_symbol",
                        columnList = "exchange,tradingSymbol",
                        unique = true),

                @Index(name = "idx_token",
                        columnList = "instrumentToken")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Zerodha Instrument Token
     */
    @Column(nullable = false)
    private Long instrumentToken;

    /**
     * Exchange
     * NSE
     * NFO
     */
    @Column(nullable = false, length = 20)
    private String exchange;

    /**
     * Trading Symbol
     */
    @Column(nullable = false, length = 100)
    private String tradingSymbol;

    /**
     * Instrument Name
     */
    @Column(length = 200)
    private String name;

    /**
     * Segment
     * NSE
     * NFO-OPT
     * NFO-FUT
     */
    @Column(length = 50)
    private String segment;

    /**
     * Instrument Type
     * EQ
     * FUT
     * CE
     * PE
     */
    @Column(length = 20)
    private String instrumentType;

    /**
     * Expiry
     */
    private LocalDate expiry;

    /**
     * Strike Price
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal strike;

    /**
     * Tick Size
     */
    @Column(precision = 10, scale = 4)
    private BigDecimal tickSize;

    /**
     * Lot Size
     */
    private Integer lotSize;

}