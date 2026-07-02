package com.example.portfolio.trade;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public Trade createTrade(@RequestBody CreateTradeRequest request) {
        return tradeService.save(request);
    }

    @GetMapping
    public List<Trade> getTrades() {
        return tradeService.findAll();
    }
}
