package agents;

import jade.core.AID;
import utils.Point;

public class Journey extends Job {

    private Point startPoint;
    private Point endPoint;
    private int duration;
    private MachineAgent startMachine;
    private MachineAgent endMachine;


    public Journey(AID product, AID worker, int startTime, int endTime) {
        super(product, worker, startTime, endTime);
    }
}
