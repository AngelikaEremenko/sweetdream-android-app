package com.example.sweetdream.model;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class UserModel {
    private String phone;
    private String username;
    private String email;
    private Boolean news_email;
    private Boolean news_phone;
    private String address;
    private List<OrderModel> orders = new ArrayList<>();
    private Timestamp createdTimestamp;
    private Boolean isManager;

    public UserModel() {
        this.orders = new ArrayList<>();
    }

    public UserModel(String phone, String username, Timestamp createdTimestamp, Boolean news_email, Boolean news_phone, String address, Boolean isManager) {
        this();
        this.phone = phone;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.news_email = news_email;
        this.news_phone = news_phone;
        this.address = address;
        this.isManager = isManager;
    }

    public Boolean getManager() {
        return isManager;
    }

    public void setManager(Boolean manager) {
        isManager = manager;
    }

    // Метод для добавления нового заказа
    public void addOrder(OrderModel order) {
        if (orders == null) {
            orders = new ArrayList<>();
        }
        orders.add(order);
    }

    // Метод для очистки списка заказов
    public void clearOrders() {
        if (orders != null) {
            orders.clear();
        }
    }

    // Геттер для списка заказов (возвращает неизменяемый список)
    public List<OrderModel> getOrders() {
        return orders != null ? orders : new ArrayList<>();
    }

    // Сеттер для списка заказов
    public void setOrders(List<OrderModel> orders) {
        this.orders = orders != null ? new ArrayList<>(orders) : new ArrayList<>();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getNews_email() {
        return news_email;
    }

    public Boolean getNews_phone() {
        return news_phone;
    }

    public void setNews_email(Boolean news_email) {
        this.news_email = news_email;
    }

    public void setNews_phone(Boolean news_phone) {
        this.news_phone = news_phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
