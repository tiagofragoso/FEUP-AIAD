import agents.MachineAgent;
import agents.ProductAgent;
import agents.RobotAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import utils.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Factory {

    private static Point pickupPoint;
    private static Point dropoffPoint;
    private static ArrayList<String[]> products = new ArrayList<>();
    private static ArrayList<Pair<HashMap<String, Integer>, Point>> machines = new ArrayList<>();
    private static ArrayList<Pair<Integer, Point>> robots = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: sh run.sh <filename> [--verbose]");
            return;
        }

        if (args.length > 1 && args[1].equals("--verbose")) {
            LoggableAgent.severeOnly = false;
        }

        FileReader fr = new FileReader("../examples/" + args[0]);
        fr.parse();
        pickupPoint = fr.getPickupPoint();
        dropoffPoint = fr.getDropoffPoint();
        products = fr.getProducts();
        machines = fr.getMachines();
        robots = fr.getRobots();
        final DateFormat df = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        PlatformManager.getInstance().setOutputFile("../logs/" + df.format(new Date(System.currentTimeMillis())) + ".log");
        logInitialState();
        initJADE();
    }

    private static void logInitialState() {
        synchronized (PlatformManager.getInstance().out()) {
            PrintWriter out = PlatformManager.getInstance().out();
            out.println("Pickup point " + pickupPoint);
            out.println("Dropoff point " + pickupPoint);
            out.println("------------------------");
            out.println("AGENTS:\n");
            out.println("Products");
            for (int i = 0; i < products.size(); i++) {
                String[] product = products.get(i);
                out.println("Product " + i + ": " + String.join("", product));
            }
            out.println();
            out.println("Machines");
            for (int i = 0; i < machines.size(); i++) {
                Pair<HashMap<String, Integer>, Point> machine = machines.get(i);
                StringBuilder strb = new StringBuilder();
                for (Map.Entry<String, Integer> entry: machine.left.entrySet()) {
                    strb.append(entry.getKey() + ":" + entry.getValue());
                }
                out.println("Machine " + i + ": " + strb.toString() + " | Location " + machine.right);
            }
            out.println();
            out.println("Robots");
            for (int i = 0; i < robots.size(); i++) {
                Pair<Integer, Point> robot = robots.get(i);
                out.println("Robot " + i + ": Velocity: " + robot.left + " | Starting location:  " + robot.right);
            }
            out.println();
            out.println("------------------------");
            out.println("RESULTS:\n\n");
            out.flush();
        }
    }

    private static void initJADE() {
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        ContainerController mainContainer = rt.createMainContainer(p1);
        setup(mainContainer);
        rt.shutDown();
    }



    private static void setup(ContainerController mainContainer) {

        try {
            System.out.println(mainContainer.getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        generateProducts(products, mainContainer);
        generateMachines(machines, mainContainer);
        generateRobots(robots, mainContainer);
    }

    private static void generateProducts(ArrayList<String[]> products, ContainerController mainContainer) {
        PlatformManager.getInstance().setProductCount(products.size());
        for (int i = 0; i < products.size(); i++) {
            String[] p = products.get(i);
            try {
                ProductAgent pr = new ProductAgent(p, Factory.pickupPoint, Factory.dropoffPoint);
                AgentController ac1 = mainContainer.acceptNewAgent("Product " + i, pr);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private static void generateMachines(ArrayList<Pair<HashMap<String, Integer>, Point>> machines, ContainerController mainContainer) {
        for (int i = 0; i < machines.size(); i++) {
            HashMap<String, Integer> m = machines.get(i).left;
            Point location = machines.get(i).right;
            try {
                MachineAgent ma = new MachineAgent(m, location);
                AgentController ac1 = mainContainer.acceptNewAgent("Machine " + i, ma);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private static void generateRobots(ArrayList<Pair<Integer, Point>> robots, ContainerController mainContainer) {
        for (int i = 0; i < robots.size(); i++) {
            int velocity = robots.get(i).left;
            Point location = robots.get(i).right;
            try {
                RobotAgent ma = new RobotAgent(velocity, location);
                AgentController ac1 = mainContainer.acceptNewAgent("Robot " + i, ma);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

}
