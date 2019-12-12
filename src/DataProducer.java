import utils.Pair;
import utils.PlatformManager;
import utils.Point;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DataProducer {

    PrintWriter printWriter;
    Point pickup = new Point(0,5);
    Point dropoff = new Point(30, 5);
    int width = 30;
    int height = 10;

    public static void main(String[] args) {

        DataProducer dataProducer = new DataProducer();

        try {
            dataProducer.openFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int numberMachines = Integer.parseInt(args[0]);
        int numberRobots = Integer.parseInt(args[1]);
        int velocity = Integer.parseInt(args[2]);
        int duration = Integer.parseInt(args[3]);
        dataProducer.run(numberMachines, numberRobots, velocity, duration);
    }

    private void openFile() throws IOException {
        File file= new File ("../logs/rapidData.csv");
        FileWriter fw;
        if (file.exists()){
            fw = new FileWriter(file,true);
        }
        else{
            file.createNewFile();
            fw = new FileWriter(file);
        }

        printWriter = new PrintWriter(fw);
    }

     private void run(int numberMachines, int numberRobots, int velocity, int duration) {
        Factory.setVariables(pickup, dropoff, generateMachines(numberMachines, duration), generateProducts(), generateRobots(numberRobots, velocity));
        Factory.initJADE();
        while(!PlatformManager.getInstance().isFinished()){
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int maxTime = PlatformManager.getInstance().maxTime;
         DecimalFormat df = new DecimalFormat("0.00");
         DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
         dfs.setDecimalSeparator('.');
         df.setDecimalFormatSymbols(dfs);
        double occupation = PlatformManager.getInstance().machineTime / (double) numberMachines / maxTime * 100;

        printWriter.write(numberMachines+","+numberRobots+","+velocity+","+duration+",");
        printWriter.write(maxTime+","+ df.format(occupation) +"\n");
        printWriter.close();
        System.exit(0);
    }

    private ArrayList<String[]> generateProducts() {
        ArrayList<String[]> products = new ArrayList<>();
        String[] processes0 = {"A", "B"};
        String[] processes1 = {"B", "C", "D"};
        String[] processes2 = {"C", "E", "A", "B", "D"};
        products.add(processes0);
        products.add(processes0);
        products.add(processes0);
        products.add(processes0);
        products.add(processes0);
        products.add(processes1);
        products.add(processes1);
        products.add(processes1);
        products.add(processes1);
        products.add(processes1);
        products.add(processes2);
        products.add(processes2);
        products.add(processes2);
        products.add(processes2);
        products.add(processes2);
        return products;
    }

    private ArrayList<Pair<HashMap<String, Integer>, Point>> generateMachines(int number, int duration) {
        ArrayList<Pair<HashMap<String, Integer>, Point>> machines = new ArrayList<>();
        int widthIncrement = width / (int) Math.ceil(number/2.0);
        int heightIncrement = height/2;
        HashMap<String, Integer> processes = new HashMap<>();
        processes.put("A", duration);
        processes.put("B", duration);
        processes.put("C", duration);
        processes.put("D", duration);
        processes.put("E", duration);
        for (int i = 0; i < number; i++) {
            machines.add(new Pair<>(processes, new Point(i*widthIncrement, ((i+1)*heightIncrement) % height)));
        }

        return machines;
    }

    private ArrayList<Pair<Integer, Point>> generateRobots(int number, int velocity) {
        ArrayList<Pair<Integer, Point>> robots = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            robots.add(new Pair<>(velocity, pickup));
        }

        return robots;
    }

}
