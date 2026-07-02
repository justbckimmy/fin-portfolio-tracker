package com.example.portfolio.holding;

import java.math.BigDecimal;

public record HoldingResponse(
        String ticker,
        String stockName,
        int quantity,
        BigDecimal averageBuyPrice,
        BigDecimal totalBuyAmount
) {
}
