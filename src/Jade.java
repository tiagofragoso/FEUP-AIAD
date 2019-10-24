import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class Jade {
    public static void main(String[] args) {
        initJADE();
    }

    public static void initJADE(){
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        ContainerController mainContainer = rt.createMainContainer(p1);
        setup(mainContainer);
        rt.shutDown();
    }

    private static void setup(ContainerController mainContainer) {
        int numMachines = 3;
        int numProducts = 2;

        try {
            System.out.println(mainContainer.getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < numMachines; i++){
            try {
                MachineAgent ma = new MachineAgent();
                AgentController ac1 = mainContainer.acceptNewAgent("Machine " + i, ma);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < numProducts; i++){
            try {
                String[] p = { "A", "B", "C" };
                ProductAgent pr = new ProductAgent(p, 20);
                AgentController ac1 = mainContainer.acceptNewAgent("Product " + i, pr);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

}
