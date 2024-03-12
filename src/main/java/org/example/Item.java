package org.example;

public class Item {
    private String name;
    private int count;
    private int price;
    private int time;

    public Item(String name, int count, int price, int time) {
        this.name = name;
        this.count = count;
        this.price = price;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void take(int number) {
        count -= number;
    }

    @Override
    public String toString() {
        return name + ". Count: " + count + " price: " + price + " time: " + time;
    }
}
