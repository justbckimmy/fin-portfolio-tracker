package com.example.portfolio.portfolio;

import com.example.portfolio.holding.HoldingResponse;
import com.example.portfolio.holding.HoldingService;
import com.example.portfolio.portfolio.PortfolioService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PortfolioService {

    private final HoldingService holdingService;

    public PortfolioService(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    public PortfolioSummaryResponse getSummary() {
        List<HoldingResponse> holdings = holdingService.findAll();

        int totalHoldingCount = holdings.size();

        BigDecimal totalBuyAmount = BigDecimal.ZERO;

        for(HoldingResponse holding : holdings) {
            totalBuyAmount = totalBuyAmount.add(holding.totalBuyAmount());
        }

        return new PortfolioSummaryResponse(
                totalHoldingCount,
                totalBuyAmount
        );
    }
}
