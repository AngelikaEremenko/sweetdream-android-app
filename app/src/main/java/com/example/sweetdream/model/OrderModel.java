package com.example.sweetdream.model;

import java.util.List;

public class OrderModel {
    private String orderId;
    private List<CartModel> items;
    private int total;
    private String timestamp;
    private String status;

    // Пустой конструктор (обязателен для Firebase)
    public OrderModel() {}

    // Конструктор
    public OrderModel(String orderId, List<CartModel> items, int total, String timestamp, String status) {
        this.orderId = orderId;
        this.items = items;
        this.total = total;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Геттеры и сеттеры
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public List<CartModel> getItems() { return items; }
    public void setItems(List<CartModel> items) { this.items = items; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
