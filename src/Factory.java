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
import utils.Pair;
import utils.Point;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Factory {

    final static Point pickupPoint = new Point(0, 5);
    final static Point dropoffPoint = new Point(30, 5);

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

        try {
            System.out.println(mainContainer.getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }


        generateMachines(
                new ArrayList<Pair<HashMap<String, Integer>, Point>>() {{
                    add(
                            new Pair<>(
                                new HashMap<String, Integer>() {{
                                    put("A", 10);
                                    put("B", 20);
                                    put("C", 30);
                                }},
                                    new Point(10, 0)
                            )
                    );
                    /*add(
                            new Pair<>(
                                    new HashMap<String, Integer>() {{
                                        put("A", 10);
                                        put("B", 20);
                                    }},
                                    new Point(10, 10)
                            )
                    );*/

                }},
                mainContainer
        );

        generateProducts(
                new String[][]{
                        new String[]{"A", "B", "C"},
                        new String[]{"A", "B"},
                        /*new String[]{"A", "B", "C"},
                        new String[]{"A"},
                        //new String[]{"A", "B", "C", "D", "E"},
                        new String[]{"A", "B",},
                        new String[]{"A", "C"},
                        new String[]{"C"},*/
                },
                mainContainer);

        generateRobots(
                new ArrayList<Pair<Integer, Point>>() {{
                    add(new Pair<>(5, new Point(10, 5)));
/*                    add(new Pair<>(10, new Point(6, 5)));
                    add(new Pair<>(5, new Point(12, 0)));
                    add(new Pair<>(5, new Point(12, 10)));*/
                }},
                mainContainer
        );

    }

    private static void generateProducts(String[][] products, ContainerController mainContainer) {
        for (int i = 0; i < products.length; i++) {
            String[] p = products[i];
            try {
                ProductAgent pr = new ProductAgent(p, Factory.pickupPoint);
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
