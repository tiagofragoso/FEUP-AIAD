import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class jade {
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
        int numProducts = 5;

        try {
            System.out.println(mainContainer.getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < numMachines; i++){
            try {
                AgentMachine ma = new AgentMachine();
                AgentController ac1 = mainContainer.acceptNewAgent("Machine " + i, ma);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < numProducts; i++){
            try {
                AgentProduct pr = new AgentProduct();
                AgentController ac1 = mainContainer.acceptNewAgent("Product " + i, pr);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

}
