package com.example.sweetdream.model;

import java.io.Serializable;

public class CakeModel implements Serializable {
    String name;
    String price;
    String img_url;
    String id;

    public CakeModel() {
    }

    public CakeModel(String img_url, String name, String price, String id) {
        this.name = name;
        this.price = price;
        this.img_url = img_url;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }


    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
