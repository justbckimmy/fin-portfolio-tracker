package com.example.portfolio.holding;

import com.example.portfolio.stock.Stock;
import com.example.portfolio.trade.Trade;
import com.example.portfolio.trade.TradeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HoldingService {

    private final TradeRepository tradeRepository;

    public HoldingService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public List<HoldingResponse> findAll() {
        List<Trade> trades = tradeRepository.findAll();

        Map<Long, Integer> quantityMap = new LinkedHashMap<>();
        Map<Long, BigDecimal> totalBuyAmountMap = new LinkedHashMap<>();
        Map<Long, Stock> stockMap = new LinkedHashMap<>();

        for (Trade trade : trades) {
            Stock stock = trade.getStock();
            Long stockId = stock.getId();

            int quantity = trade.getQuantity();

            if (trade.getType().equals("BUY")) {
                BigDecimal buyAmount = trade.getPrice().multiply(BigDecimal.valueOf(quantity));

                quantityMap.put(stockId, quantityMap.getOrDefault(stockId, 0) + quantity);
                totalBuyAmountMap.put(
                        stockId,
                        totalBuyAmountMap.getOrDefault(stockId, BigDecimal.ZERO).add(buyAmount)
                );
                stockMap.put(stockId, stock);
            }

            if (trade.getType().equals("SELL")) {
                int currentQuantity = quantityMap.getOrDefault(stockId, 0);
                BigDecimal currentTotalBuyAmount = totalBuyAmountMap.getOrDefault(stockId, BigDecimal.ZERO);

                if (currentQuantity > 0) {
                    BigDecimal currentAverageBuyPrice = currentTotalBuyAmount.divide(
                            BigDecimal.valueOf(currentQuantity),
                            2,
                            RoundingMode.HALF_UP
                    );

                    BigDecimal sellCostAmount = currentAverageBuyPrice.multiply(BigDecimal.valueOf(quantity));

                    quantityMap.put(stockId, currentQuantity - quantity);
                    totalBuyAmountMap.put(stockId, currentTotalBuyAmount.subtract(sellCostAmount));
                }

                stockMap.put(stockId, stock);
            }
        }

        List<HoldingResponse> responses = new ArrayList<>();

        for (Long stockId : quantityMap.keySet()) {
            Stock stock = stockMap.get(stockId);
            int quantity = quantityMap.get(stockId);

            if (quantity <= 0) {
                continue;
            }

            BigDecimal totalBuyAmount = totalBuyAmountMap.get(stockId);

            BigDecimal averageBuyPrice = totalBuyAmount.divide(
                    BigDecimal.valueOf(quantity),
                    2,
                    RoundingMode.HALF_UP
            );

            HoldingResponse response = new HoldingResponse(
                    stock.getTicker(),
                    stock.getName(),
                    quantity,
                    averageBuyPrice,
                    totalBuyAmount
                    );

            responses.add(response);

        }

        return responses;
    }
}
