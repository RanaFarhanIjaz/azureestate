package com.example.azureestate.models;

public class StockModel {
    private String symbol;
    private String name;
    private double price;
    private double change;
    private String marketCap;
    private double changePercent;

    public StockModel(String symbol, String name, double price, double change, double changePercent, String marketCap) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
        this.marketCap = marketCap;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getChange() { return change; }
    public double getChangePercent() { return changePercent; }
    public String getMarketCap() { return marketCap; }
}
