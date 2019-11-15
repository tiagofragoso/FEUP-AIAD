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
import utils.LoggableAgent;
import utils.Point;

import java.util.ArrayList;
import java.util.HashMap;

public class Factory {

    final Point pickupPoint = new Point(0, 5);
    final Point dropoffPoint = new Point(30, 5);

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--verbose")) {
            LoggableAgent.severeOnly = false;
        }
        initJADE();
    }

    private static void initJADE() {
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        ContainerController mainContainer = rt.createMainContainer(p1);
        setup(mainContainer);
        rt.shutDown();
    }

    private static void setup(ContainerController mainContainer) {
        int numMachines = 1;
        int numProducts = 1;
        int numRobots = 1;

        try {
            System.out.println(mainContainer.getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < numRobots; i++) {
            try {
                RobotAgent ra = new RobotAgent();
                ra.setStartingPoint(new Point(10, 10));
                AgentController ac1 = mainContainer.acceptNewAgent("Robot " + i, ra);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < numMachines; i++) {
            try {
                MachineAgent ma = new MachineAgent(new Point(15, 5));
                ma.addAvailableProcess("A", 30);
                ma.addAvailableProcess("B", 30);
                AgentController ac1 = mainContainer.acceptNewAgent("Machine " + i, ma);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

        /*generateMachines(
                new ArrayList<HashMap<String, Integer>>() {{
                    add(
                            new HashMap<String, Integer>() {{
                                put("A", 10);
                                put("B", 20);
                                put("C", 10);
                            }}
                    );
                    add(
                            new HashMap<String, Integer>() {{
                                put("A", 20);
                                put("C", 5);
                            }}
                    );
                    add(
                            new HashMap<String, Integer>() {{
                                put("D", 10);
                                put("E", 100);
                            }}
                    );
                }},
                mainContainer
        );*/

        /*generateProducts(
                new String[][]{
                        new String[]{"A", "B", "C"},
                        new String[]{"A", "B"},
                        new String[]{"A", "B", "C", "D"},
                        new String[]{"A"},
                        new String[]{"A", "B", "C", "D", "E"},
                        new String[]{"A", "B",},
                        new String[]{"A", "C"},
                        new String[]{"C"},
                },
                mainContainer);*/

        for (int i = 0; i < numProducts; i++) {
            try {
                String[] p = {"A", "B"};
                ProductAgent pr = new ProductAgent(p, 20);
                AgentController ac1 = mainContainer.acceptNewAgent("Product " + i, pr);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private static void generateProducts(String[][] products, ContainerController mainContainer) {
        for (int i = 0; i < products.length; i++) {
            String[] p = products[i];
            try {
                ProductAgent pr = new ProductAgent(p, 20);
                AgentController ac1 = mainContainer.acceptNewAgent("Product " + i, pr);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private static void generateMachines(ArrayList<HashMap<String, Integer>> machines, ContainerController mainContainer) {
        for (int i = 0; i < machines.size(); i++) {
            HashMap<String, Integer> m = machines.get(i);
            try {
                MachineAgent ma = new MachineAgent(m);
                AgentController ac1 = mainContainer.acceptNewAgent("Machine " + i, ma);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

}
