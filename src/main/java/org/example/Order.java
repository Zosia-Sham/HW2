package org.example;

import java.util.ArrayDeque;
import java.util.concurrent.Callable;

public class Order implements Callable<Integer> {
    private ArrayDeque<Dish> dishQueue;
    private Guest guest;
    private int bill;
    private boolean paid;

    public Order(ArrayDeque<Dish> dishes, Guest guest) {
        dishQueue = dishes;
        this.guest = guest;
        bill =  0;
        paid = false;
        System.out.println("Status : latest order of " + guest.getName() + " is confirmed.");
    }

    @Override
    public Integer call() {
        System.out.println("Status : latest order of " + guest.getName() + " is cooking.");
        boolean dishesDone = false;
        while (!Thread.currentThread().isInterrupted() && !dishesDone) {
            try {
                Thread.sleep(1000 * dishQueue.getFirst().getTime());
                bill += dishQueue.getFirst().getPrice();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            synchronized(dishQueue) {
                dishQueue.pop();
                dishesDone = dishQueue.isEmpty();
            }
        }
        if (Thread.currentThread().isInterrupted()) {
            System.out.println("Status : latest order of " + guest.getName() + " is canceled.");
        } else {
            System.out.println("Status : latest order of " + guest.getName() + " is completed.");
        }
        return bill;
    }

    boolean addDishes(ArrayDeque<Dish> dishes) {
        synchronized(dishQueue) {
            if (!dishQueue.isEmpty()) {
                for (Dish dish : dishes) {
                    dishQueue.push(dish);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isPaid() {
        return paid;
    }
}