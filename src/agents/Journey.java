package agents;

import utils.Point;

public class Journey {

    private Point startPoint;
    private Point endPoint;
    private int startTime;
    private int endTime;
    private int durationTime;
    private ProductAgent product;
    private MachineAgent startMachine;
    private MachineAgent endMachine;

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(int durationTime) {
        this.durationTime = durationTime;
    }

    public ProductAgent getProduct() {
        return product;
    }

    public void setProduct(ProductAgent product) {
        this.product = product;
    }

    public MachineAgent getStartMachine() {
        return startMachine;
    }

    public void setStartMachine(MachineAgent startMachine) {
        this.startMachine = startMachine;
    }

    public MachineAgent getEndMachine() {
        return endMachine;
    }

    public void setEndMachine(MachineAgent endMachine) {
        this.endMachine = endMachine;
    }

}
