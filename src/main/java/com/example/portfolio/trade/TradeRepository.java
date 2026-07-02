package com.example.portfolio.trade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long>{
    List<Trade> findByStockId(Long stockId);
}
