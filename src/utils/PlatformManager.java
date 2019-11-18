package utils;

import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

public class PlatformManager {
    private static PlatformManager instance = null;
    private ContainerController mainContainer;
    private int maxTime = 0;
    private int unfinishedProducts;

    synchronized public void registerProductTime(int time) {
        maxTime = Math.max(maxTime, time);
        unfinishedProducts--;
        if (unfinishedProducts <= 0) {
            System.out.println("Finished at " + maxTime);
            System.out.println("Press CTRL/CMD+C to exit\n");
        }
    }

    public void setProductCount(int unfinishedProducts) {
        this.unfinishedProducts = unfinishedProducts;
    }

    private void killAll() {
        try {
            this.mainContainer.getPlatformController().kill();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    public void setMainContainer(ContainerController mainContainer) {
        this.mainContainer = mainContainer;
    }

    public ContainerController getMainContainer() {
        return mainContainer;
    }

    public static PlatformManager getInstance() {
        if (instance == null) {
            instance = new PlatformManager();
        }
        return instance;
    }
}
