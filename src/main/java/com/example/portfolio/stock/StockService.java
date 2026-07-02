package com.example.portfolio.stock;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Stock save(Stock stock) {
        stockRepository.findByTicker((stock.getTicker()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("이미 등록된 종목입니다.:" + stock.getTicker());
                });
        return stockRepository.save(stock);
    }

    public List<Stock> findAll() {
        return stockRepository.findAll();
    }
}
