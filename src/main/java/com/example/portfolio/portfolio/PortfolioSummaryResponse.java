package com.example.portfolio.portfolio;

import java.math.BigDecimal;

public record PortfolioSummaryResponse(
        int totalHoldingCount,
        BigDecimal totalBuyAmount
) {
}
