package org.example;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.concurrent.Future;

public class Guest implements User {
    private String name;
    private int tableNumber;
    private ArrayList<SimpleEntry<Order, Future<Integer>>> orders;

    public Guest(String name, int tableNumber) {
        this.name = name;
        this.tableNumber = tableNumber;
        orders = new ArrayList<SimpleEntry<Order, Future<Integer>>>();
    }

    public boolean hasOrders() {
        return !orders.isEmpty();
    }

    public void addOrder(Order order, Future<Integer> result) {
        orders.add(new SimpleEntry<>(order, result));
    }

    public Order getLatestOrder() {
        return orders.getLast().getKey();
    }

    public Future<Integer> getLatestOrderResult() {
        return orders.getLast().getValue();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<SimpleEntry<Order, Future<Integer>>> getOrders() {
        return orders;
    }
}
