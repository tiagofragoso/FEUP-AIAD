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

import java.util.ArrayList;
import java.util.HashMap;

public class Factory {

    private static Point pickupPoint;
    private static Point dropoffPoint;
    private static ArrayList<String[]> products = new ArrayList<>();
    private static ArrayList<Pair<HashMap<String, Integer>, Point>> machines = new ArrayList<>();
    private static ArrayList<Pair<Integer, Point>> robots = new ArrayList<>();


    public static void main(String[] args) {
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
        initJADE();
    }

    private static void initJADE() {
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        ContainerController mainContainer = rt.createMainContainer(p1);
        PlatformManager.getInstance().setMainContainer(mainContainer);
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
