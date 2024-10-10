package com.example.wattescu.entities;

public class Provider {

    private String name;
    private Double priceKw;

    public Provider() {
    }

    public Provider(String name, Double priceKw) {
        this.name = name;
        this.priceKw = priceKw;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPriceKw() {
        return priceKw;
    }

    public void setPriceKw(Double priceKw) {
        this.priceKw = priceKw;
    }

    @Override
    public String toString() {
        return "Provider{" +
                "name='" + name + '\'' +
                ", priceKw=" + priceKw +
                '}';
    }
}
