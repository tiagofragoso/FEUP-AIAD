package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class FileReader {
    private Scanner scanner;
    private parse_state state = parse_state.PICKUP;

    private enum parse_state {PICKUP, DROPOFF, AGENTS, PRODUCTS, MACHINES, ROBOTS, DONE}

    private int productCount = 0;
    private int machineCount = 0;
    private int robotCount = 0;
    private Point pickupPoint;
    private Point dropoffPoint;
    private ArrayList<String[]> products = new ArrayList<>();
    private ArrayList<Pair<HashMap<String, Integer>, Point>> machines = new ArrayList<>();
    private ArrayList<Pair<Integer, Point>> robots = new ArrayList<>();


    public FileReader(String fileName) {
        File directory = new File("./");
        System.out.println(directory.getAbsolutePath());
        File file = new File(fileName);
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void parse() {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (state == parse_state.DONE) {
                scanner.close();
                return;
            }
            if (line.startsWith("#"))
                continue;
            action(line);
        }
    }

    void action(String line) {
        switch (state) {
            case PICKUP:
                parsePickup(line);
                break;
            case DROPOFF:
                parseDropoff(line);
                break;
            case AGENTS:
                parseAgents(line);
                break;
            case PRODUCTS:
                parseProduct(line);
                break;
            case MACHINES:
                parseMachine(line);
                break;
            case ROBOTS:
                parseRobot(line);
                break;

        }
    }

    private void parsePickup(String line) {
        this.pickupPoint = parsePoint(line);
        state = parse_state.DROPOFF;
    }

    private void parseDropoff(String line) {
        this.dropoffPoint = parsePoint(line);
        state = parse_state.AGENTS;
    }

    private void parseAgents(String line) {
        String[] tokens = line.split(";");
        this.productCount = Integer.parseInt(tokens[0]);
        this.machineCount = Integer.parseInt(tokens[1]);
        this.robotCount = Integer.parseInt(tokens[2]);
        state = parse_state.PRODUCTS;
    }

    private void parseProduct(String line) {
        productCount--;
        String[] processes = line.split(",");
        this.products.add(processes);
        if (productCount <= 0)
            state = parse_state.MACHINES;
    }

    private void parseMachine(String line) {
        machineCount--;
        String[] tokens = line.split(";");
        String[] process_duration = tokens[0].split(",");
        HashMap<String, Integer> processes = new HashMap<>();
        for (String s : process_duration) {
            String[] pdtokens = s.split(":");
            processes.put(pdtokens[0], Integer.parseInt(pdtokens[1]));
        }
        this.machines.add(new Pair<>(processes, parsePoint(tokens[1])));
        if (machineCount <= 0)
            state = parse_state.ROBOTS;
    }

    private void parseRobot(String line) {
        robotCount--;
        String[] tokens = line.split(";");
        this.robots.add(new Pair<>(Integer.parseInt(tokens[0]), parsePoint(tokens[1])));
        if (robotCount <= 0) {
            state = parse_state.DONE;
        }
    }

    private Point parsePoint(String line) {
        String[] tokens = line.split(",");
        return new Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
    }

    public Point getPickupPoint() {
        return pickupPoint;
    }

    public Point getDropoffPoint() {
        return dropoffPoint;
    }

    public ArrayList<String[]> getProducts() {
        return products;
    }

    public ArrayList<Pair<HashMap<String, Integer>, Point>> getMachines() {
        return machines;
    }

    public ArrayList<Pair<Integer, Point>> getRobots() {
        return robots;
    }
}
