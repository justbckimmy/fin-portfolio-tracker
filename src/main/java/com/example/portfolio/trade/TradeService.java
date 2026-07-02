package com.example.portfolio.trade;

import com.example.portfolio.stock.Stock;
import com.example.portfolio.stock.StockRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final StockRepository stockRepository;

    public TradeService(TradeRepository tradeRepository, StockRepository stockRepository) {
        this.tradeRepository = tradeRepository;
        this.stockRepository = stockRepository;
    }

    public Trade save(CreateTradeRequest request) {

        if (request.stockId() == null) {
            throw new IllegalArgumentException("종목 id는 필수입니다.");
        }

        if (request.type() == null || (!request.type().equals("BUY") && !request.type().equals("SELL"))) {
            throw new IllegalArgumentException("거래 유형은 BUY 또는 SELL만 가능합니다.");
        }

        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }

        Stock stock = stockRepository.findById(request.stockId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 종목입니다.")) ;

        if (request.type().equals("SELL")) {
            int currentQuantity = getCurrentQuantity(stock.getId());
            if (request.quantity() > currentQuantity) {
                throw new IllegalArgumentException(
                        "보유 수량 (" + currentQuantity + "주)보다 많은 수량은 매도할 수 없습니다."
                );
            }
        }

        Trade trade = new Trade(
                stock,
                request.type(),
                request.quantity(),
                request.price(),
                request.tradeDate() == null ? LocalDate.now() : request.tradeDate()

        );

        return tradeRepository.save(trade);
    }

    private int getCurrentQuantity(Long stockId) {
        List<Trade> trades = tradeRepository.findByStockId(stockId);

        int quantity = 0;
        for (Trade trade : trades) {
            if (trade.getType().equals("BUY")) {
                quantity += trade.getQuantity();
            } else if (trade.getType().equals("SELL")) {
                quantity -= trade.getQuantity();
            }
        }
        return quantity;
    }

    public List<Trade> findAll() {
        return tradeRepository.findAll();
    }
}
