package com.example.portfolio.trade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTradeRequest (
        Long stockId,
        String type,
        int quantity,
        BigDecimal price,
        LocalDate tradeDate
) {
}
