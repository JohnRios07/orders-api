package com.restaurant.orders.dto;

import java.math.BigDecimal;

public class OrderItem {

    private String name;
    private String variant;
    private int qty;
    private BigDecimal price;
    private BigDecimal lineTotal;

    // Constructors
    public OrderItem() {}

    public OrderItem(String name, String variant, int qty, BigDecimal price, BigDecimal lineTotal) {
        this.name = name;
        this.variant = variant;
        this.qty = qty;
        this.price = price;
        this.lineTotal = lineTotal;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}