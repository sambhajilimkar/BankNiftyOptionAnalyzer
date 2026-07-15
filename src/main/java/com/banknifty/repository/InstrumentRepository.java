package com.banknifty.repository;

import com.banknifty.entity.InstrumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository
        extends JpaRepository<InstrumentEntity, Long> {

    Optional<InstrumentEntity> findByExchangeAndTradingSymbol(
            String exchange,
            String tradingSymbol
    );

    Optional<InstrumentEntity> findByInstrumentToken(
            Long instrumentToken
    );

    List<InstrumentEntity> findBySegment(
            String segment
    );

    List<InstrumentEntity> findByExpiry(
            LocalDate expiry
    );

    List<InstrumentEntity> findByNameContainingIgnoreCase(
            String name
    );

    List<InstrumentEntity> findByExchange(
            String exchange
    );

}