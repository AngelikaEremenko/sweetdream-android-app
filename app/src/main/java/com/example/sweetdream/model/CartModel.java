package com.example.sweetdream.model;

public class CartModel {
    String name;
    String price;
    String img_url;
    String weight;
    String amount;

    public CartModel(String name, String price, String img_url, String weight, String amount) {
        this.name = name;
        this.price = price;
        this.img_url = img_url;
        this.weight = weight;
        this.amount = amount;
    }

    public CartModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
