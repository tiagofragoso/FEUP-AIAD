package utils;

import jade.wrapper.ContainerController;

import java.io.IOException;
import java.io.PrintWriter;

public class PlatformManager {
    private static PlatformManager instance = null;
    private int maxTime = 0;
    private int unfinishedProducts;
    private FileWriter out;

    public void setOutputFile(String fileName) throws IOException {
        this.out = new FileWriter(fileName);
    }

    public PrintWriter out() {
        return this.out.out();
    }

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

    public static PlatformManager getInstance() {
        if (instance == null) {
            instance = new PlatformManager();
        }
        return instance;
    }
}
