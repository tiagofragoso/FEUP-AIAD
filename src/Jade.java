import agents.MachineAgent;
import agents.ProductAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import utils.LoggableAgent;

public class Jade {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--verbose")) {
            LoggableAgent.severeOnly = false;
        }
        initJADE();
    }

    public static void initJADE() {
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        ContainerController mainContainer = rt.createMainContainer(p1);
        setup(mainContainer);
        rt.shutDown();
    }

    private static void setup(ContainerController mainContainer) {
        int numMachines = 2;
        int numProducts = 2;

        try {
            System.out.println(mainContainer.getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < numMachines; i++) {
            try {
                MachineAgent ma = new MachineAgent();
                ma.addTask("A", 30);
                ma.addTask("B", 30);
                AgentController ac1 = mainContainer.acceptNewAgent("Machine " + i, ma);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

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

}
