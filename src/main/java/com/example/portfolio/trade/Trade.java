package com.example.portfolio.trade;

import com.example.portfolio.stock.Stock;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Stock stock;

    private String type;
    private int quantity;
    private BigDecimal price;
    private LocalDate tradeDate;

    protected Trade() {
    }

    public Trade(Stock stock, String type, int quantity, BigDecimal price, LocalDate tradeDate) {
        this.stock = stock;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.tradeDate = tradeDate;
    }

    public Long getId() {
        return id;
    }

    public Stock getStock() {
        return stock;
    }

    public String getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

}
