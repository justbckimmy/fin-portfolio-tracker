package com.example.portfolio.stock;

import jakarta.persistence.*;
import jakarta.persistence.Column;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ticker; //종목 코드 저장
    private String name; //종목 이름 저장
    private String market; //종목 시장 저장

    protected Stock() {
    }

    public Stock(String ticker, String name, String market) {
        this.ticker = ticker;
        this.name = name;
        this.market = market;
    }

    public Long getId() {
        return id;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public String getMarket() {
        return market;
    }

}
