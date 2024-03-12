package org.example;

import org.apache.commons.cli.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.concurrent.*;

public class RestaurantController {
    HashMap<String, User> users;
    User user;
    HashMap<String, Item> menu;
    ArrayDeque<Order> orders;
    final ExecutorService executor;
    int tableCounter;
    int profit;
    boolean done;
    Options addItemOptions;
    Options removeItemOptions;
    Options updateItemOptions;
    Options newOrderOptions;
    Options addToOrderOptions;
    Options signUpOptions;
    Options signInOptions;

    public RestaurantController() {
        users = new HashMap<String, User>();
        menu = new HashMap<String, Item>();
        tableCounter = 0;
        profit = 0;
        done = false;
        executor = Executors.newCachedThreadPool();
        orders = new ArrayDeque<Order>();

        addItemOptions = new Options();
        addItemOptions.addOption(Option.builder("n")
                .longOpt("name")
                .argName("name")
                .hasArg()
                .required(true)
                .desc("name of the dish").build());
        addItemOptions.addOption(Option.builder("c")
                .longOpt("count")
                .argName("count")
                .hasArg()
                .required(true)
                .desc("number of the dishes that restaurant has").build());
        addItemOptions.addOption(Option.builder("p")
                .longOpt("price")
                .argName("price")
                .hasArg()
                .required(true)
                .desc("price of the dish").build());
        addItemOptions.addOption(Option.builder("t")
                .longOpt("time")
                .argName("time")
                .hasArg()
                .required(true)
                .desc("how long it takes to cook (in seconds)").build());

        removeItemOptions = new Options();
        removeItemOptions.addOption(Option.builder("n")
                .longOpt("name")
                .argName("name")
                .hasArg()
                .required(true)
                .desc("name of the dish").build());

        updateItemOptions = new Options();
        updateItemOptions.addOption(Option.builder("n")
                .longOpt("name")
                .argName("name")
                .hasArg()
                .required(true)
                .desc("name of the dish").build());
        updateItemOptions.addOption(Option.builder("c")
                .longOpt("count")
                .argName("count")
                .hasArg()
                .required(false)
                .desc("number of the dishes that restaurant has").build());
        updateItemOptions.addOption(Option.builder("p")
                .longOpt("price")
                .argName("price")
                .hasArg()
                .required(false)
                .desc("price of the dish").build());
        updateItemOptions.addOption(Option.builder("t")
                .longOpt("time")
                .argName("time")
                .hasArg()
                .required(false)
                .desc("how long it takes to cook (in seconds)").build());

        newOrderOptions = new Options();
        newOrderOptions.addOption(Option.builder("d")
                .longOpt("dishes")
                .argName("dishes")
                .hasArg()
                .required(true)
                .desc("list of dishes in the order").build());
        newOrderOptions.getOption("dishes").setArgs(Option.UNLIMITED_VALUES);

        newOrderOptions = new Options();
        newOrderOptions.addOption(Option.builder("d")
                .longOpt("dishes")
                .argName("dishes")
                .hasArg()
                .required(true)
                .desc("space-separated dishes (names) in the order").build());
        newOrderOptions.getOption("dishes").setArgs(Option.UNLIMITED_VALUES);

        addToOrderOptions = new Options();
        addToOrderOptions.addOption(Option.builder("d")
                .longOpt("dishes")
                .argName("dishes")
                .hasArg()
                .required(true)
                .desc("space-separated dishes (names) to add to the order").build());
        addToOrderOptions.getOption("dishes").setArgs(Option.UNLIMITED_VALUES);

        signUpOptions = new Options();
        signUpOptions.addOption(Option.builder("n")
                .longOpt("name")
                .argName("name")
                .hasArg()
                .required(true)
                .desc("your name").build());
        signUpOptions.addOption(Option.builder("a")
                .longOpt("admin")
                .argName("admin")
                .hasArg(false)
                .required(false)
                .desc("type of user is admin").build());
        signUpOptions.addOption(Option.builder("g")
                .longOpt("guest")
                .argName("guest")
                .hasArg(false)
                .required(false)
                .desc("type of user is guest").build());

        signInOptions = new Options();
        signInOptions.addOption(Option.builder("n")
                .longOpt("name")
                .argName("name")
                .hasArg()
                .required(true)
                .desc("your name").build());
    }

    void run() {
        System.out.println("Hello! This is RestaurantController. Heres what you can do:");
        HelpFormatter usage = new HelpFormatter();
        System.out.println("Authorization:");
        usage.printHelp("signup", signUpOptions);
        usage.printHelp("signin", signInOptions);
        System.out.println("usage: signout");
        System.out.println("As an admin:");
        usage.printHelp("additem", addItemOptions);
        usage.printHelp("rmitem", removeItemOptions);
        usage.printHelp("upditem", updateItemOptions);
        System.out.println("usage: menu");
        System.out.println("As a guest:");
        usage.printHelp("neworder", newOrderOptions);
        usage.printHelp("add", addToOrderOptions);
        System.out.println("usage: cancel");
        System.out.println("usage: pay");
        System.out.println("usage: menu");
        System.out.println("\n---------------\n");
        System.out.println("To stop RestaurantController - command exit");

        Scanner in = new Scanner(System.in);
        String request;
        while (!done) {
            System.out.println("Please sign in or sign up");
            do {
                request = in.nextLine();
            } while (handleAuthorizationRequest(request));
            if (done) {
                break;
            }
            System.out.println("Type in your request");
            if (user instanceof Guest) {
                Guest guest = (Guest)user;
                do {
                    request = in.nextLine();
                } while (handleGuestRequest(request, guest));
            } else {
                do {
                    request = in.nextLine();
                } while (handleAdminRequest(request));
            }
        }
    }

    void addNewAdmin(String name) throws RestaurantException {
        if (users.containsKey(name)) {
            throw new RestaurantException("User with such name already exists.");
        }
        users.put(name, new Admin(name));
    }

    void addNewGuest(String name) throws RestaurantException {
        if (users.containsKey(name)) {
            throw new RestaurantException("User with such name already exists.");
        }
        users.put(name, new Guest(name, ++tableCounter));
    }

    void setUser(String name) throws RestaurantException {
        if (!users.containsKey(name)) {
            throw new RestaurantException("User with this name doesnt exist. Try signing up first.");
        }
        user = users.get(name);
    }

    void printMenu() {
        if (!menu.isEmpty()) {
            System.out.println("Menu:");
            for (Item i : menu.values()) {
                System.out.println(i);
            }
        } else {
            System.out.println("Menu is empty.");
        }
    }

    void takeOrder(String[] order, Guest guest) throws RestaurantException {
        if (guest.hasOrders() && !guest.getLatestOrderResult().isDone()) {
            throw new RestaurantException("Your latest order is still cooking. Try adding dishes there instead of creating a new order.");
        }
        HashMap<Item, Integer> items = new HashMap<Item, Integer>();
        ArrayDeque<Dish> dishes = new ArrayDeque<Dish>();
        for (String d : order) {
            if (!menu.containsKey(d)) {
                throw new RestaurantException(d + " is not on the menu.");
            }
            items.merge(menu.get(d), 1, Integer::sum);
        }
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            Item item = entry.getKey();
            if (item.getCount() < entry.getValue()) {
                throw new RestaurantException(item.getName() + " is out of stock. Sorry!");
            }
            int cnt = entry.getValue();
            while (cnt != 0) {
                dishes.add(new Dish(item.getName(), item.getPrice(), item.getTime()));
                --cnt;
            }
        }
        orders.push(new Order(dishes, guest));
        guest.addOrder(orders.peek(), executor.submit(orders.peek()));
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            entry.getKey().take(entry.getValue());
        }
    }

    void addToOrder(String[] addOn, Guest guest) throws RestaurantException {
        if (!guest.hasOrders()) {
            throw new RestaurantException("You have no orders yet. Try creating an order first.");
        }
        HashMap<Item, Integer> items = new HashMap<Item, Integer>();
        ArrayDeque<Dish> dishes = new ArrayDeque<Dish>();
        for (String d : addOn) {
            if (!menu.containsKey(d)) {
                throw new RestaurantException(d + " is not on the menu.");
            }
            items.merge(menu.get(d), 1, Integer::sum);
        }
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            Item item = entry.getKey();
            if (item.getCount() < entry.getValue()) {
                throw new RestaurantException(item.getName() + " is out of stock. Sorry!");
            }
            int cnt = entry.getValue();
            while (cnt != 0) {
                dishes.add(new Dish(item.getName(), item.getPrice(), item.getTime()));
                --cnt;
            }
        }
        if (!guest.getLatestOrder().addDishes(dishes)) {
            throw new RestaurantException("Your latest order is already completed. Try creating a new order.");
        }
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            entry.getKey().take(entry.getValue());
        }
    }

    void cancelOrder(Guest guest) throws RestaurantException {
        if (!guest.hasOrders()) {
            throw new RestaurantException("You have no orders yet. Try creating an order first.");
        }
        if (!guest.getLatestOrderResult().cancel(true)) {
            throw new RestaurantException("Your latest order is already completed (or canceled), we cant cancel it.");
        }
    }

    int pay(Guest guest) throws RestaurantException {
        int total = 0;
        if (guest.getLatestOrderResult().isDone()) {
            for (AbstractMap.SimpleEntry<Order, Future<Integer>> en : guest.getOrders()) {
                Order order = en.getKey();
                Future<Integer> result = en.getValue();
                if(order.isPaid()) {
                    continue;
                }
                try {
                    total += result.get();
                    order.setPaid(true);
                } catch (CancellationException | ExecutionException | InterruptedException e) {
                    continue;
                }
            }
            if (total != 0) {
                profit += total;
                return total;
            } else {
                throw new RestaurantException("You have no unpaid orders.");
            }
        } else {
            throw new RestaurantException("Your latest order is still cooking. Try paying when its completed.");
        }
    }

    void addItemToMenu(String name, String count, String price, String time) throws RestaurantException {
        if (menu.containsKey(name)) {
            throw new RestaurantException("Item with this name already exists. Try updating the info you want.");
        }
        try {
            menu.put(name, new Item(name, StrToInt(count), StrToInt(price), StrToInt(time)));
        } catch (NumberFormatException ex) {
            throw new RestaurantException("Numeric arguments should be positive numbers.");
        }
    }

    void removeItemFromMenu(String name) throws RestaurantException {
        if (!menu.containsKey(name)) {
            throw new RestaurantException("Item with this name doesnt exist.");
        }
        menu.remove(name);
    }

    void updateItemCount(String name, String count) throws RestaurantException {
        try {
            menu.get(name).setCount(StrToInt(count));
        } catch (NumberFormatException ex) {
            throw new RestaurantException("Numeric arguments should be positive numbers.");
        } catch (NullPointerException ex) {
            throw new RestaurantException("Item with this name doesnt exist.");
        }
    }

    void updateItemPrice(String name, String price) throws RestaurantException {
        try {
            menu.get(name).setPrice(StrToInt(price));
        } catch (NumberFormatException ex) {
            throw new RestaurantException("Numeric arguments should be positive numbers.");
        } catch (NullPointerException ex) {
            throw new RestaurantException("Item with this name doesnt exist.");
        }
    }

    void updateItemTime(String name, String time) throws RestaurantException {
        try {
            menu.get(name).setTime(StrToInt(time));
        } catch (NumberFormatException ex) {
            throw new RestaurantException("Numeric arguments should be positive numbers.");
        } catch (NullPointerException ex) {
            throw new RestaurantException("Item with this name doesnt exist.");
        }
    }

    boolean handleAuthorizationRequest(String request) {
        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        String[] ar = request.split(" ");
        if (ar.length == 0) {
            System.out.println("Type in your request");
        }
        String[] args = Arrays.copyOfRange(ar, 1, ar.length);
        try {

            switch (ar[0]) {
                case "signup":
                    cmd = parser.parse(signUpOptions, args);
                    if (cmd.hasOption("guest")) {
                        addNewGuest(cmd.getOptionValue("name"));
                        setUser(cmd.getOptionValue("name"));
                        System.out.println("Youre all signed up as a guest! Your table number is " + tableCounter);
                    } else {
                        addNewAdmin(cmd.getOptionValue("name"));
                        setUser(cmd.getOptionValue("name"));
                        System.out.println("Youre all signed up as an admin!");
                    }
                    return false;
                case "signin":
                    cmd = parser.parse(signInOptions, args);
                    setUser(cmd.getOptionValue("name"));
                    System.out.println("Hi, " + user.getName() + "!");
                    return false;
                case "exit":
                    done = true;
                    return false;
                default:
                    System.out.println("No such command. Usage");
            }

        } catch (ParseException | RestaurantException e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    boolean handleAdminRequest(String request) {
        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        String[] ar = request.split(" ");
        if (ar.length == 0) {
            System.out.println("Type in your request");
        }
        String[] args = Arrays.copyOfRange(ar, 1, ar.length);
        try {

            switch (ar[0]) {
                case "additem":
                    cmd = parser.parse(addItemOptions, args);
                    addItemToMenu(cmd.getOptionValue("name"),
                            cmd.getOptionValue("count"),
                            cmd.getOptionValue("price"),
                            cmd.getOptionValue("time"));
                    System.out.println("Done!");
                    break;
                case "rmitem":
                    cmd = parser.parse(removeItemOptions, args);
                    removeItemFromMenu(cmd.getOptionValue("name"));
                    System.out.println("Done!");
                    break;
                case "upditem":
                    cmd = parser.parse(updateItemOptions, args);
                    if (cmd.hasOption("count")) {
                        updateItemCount(cmd.getOptionValue("name"), cmd.getOptionValue("count"));
                    }
                    if (cmd.hasOption("price")) {
                        updateItemPrice(cmd.getOptionValue("name"), cmd.getOptionValue("price"));
                    }
                    if (cmd.hasOption("time")) {
                        updateItemTime(cmd.getOptionValue("name"), cmd.getOptionValue("time"));
                    }
                    if (!cmd.hasOption("count") && !cmd.hasOption("price") && !cmd.hasOption("time")) {
                        System.out.println("Missing required option : info to update");
                    } else {
                        System.out.println("Done!");
                    }
                    break;
                case "menu":
                    printMenu();
                    break;
                case "signout":
                    return false;
                case "exit":
                    done = true;
                    return false;
                default:
                    System.out.println("No such command. Usage");
            }

        } catch (ParseException | RestaurantException e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    boolean handleGuestRequest(String request, Guest guest) {
        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        String[] ar = request.split(" ");
        if (ar.length == 0) {
            System.out.println("Type in your request");
        }
        String[] args = Arrays.copyOfRange(ar, 1, ar.length);
        try {

            switch (ar[0]) {
                case "neworder":
                    cmd = parser.parse(newOrderOptions, args);
                    String[] dishes = cmd.getOptionValues("dishes");
                    takeOrder(dishes, guest);
                    System.out.println("Done!");
                    break;
                case "add":
                    cmd = parser.parse(addToOrderOptions, args);
                    dishes = cmd.getOptionValues("dishes");
                    addToOrder(dishes, guest);
                    System.out.println("Done!");
                    break;
                case "cancel":
                    cancelOrder(guest);
                    System.out.println("Done!");
                    break;
                case "pay":
                    int total = pay(guest);
                    System.out.println("Done! You paid for all your (not cancelled) orders, it was a total of " + total);
                    break;
                case "menu":
                    printMenu();
                    break;
                case "signout":
                    return false;
                case "exit":
                    done = true;
                    return false;
                default:
                    System.out.println("No such command. Usage");
            }

        } catch (ParseException | RestaurantException e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    int StrToInt(String s) throws NumberFormatException {
        int number = Integer.parseInt(s);
        if (number <= 0) {
            throw new NumberFormatException("Number is non-positive");
        }
        return number;
    }
}
