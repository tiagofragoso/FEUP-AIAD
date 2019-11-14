package agents;

import utils.Point;

import java.util.ArrayList;

public class RobotAgent {

    private int velocity;
    private ArrayList<Journey> scheduledJourneys;

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public ArrayList<Journey> getScheduledJourneys() {
        return scheduledJourneys;
    }

    public void setScheduledJourneys(ArrayList<Journey> scheduledJourneys) {
        this.scheduledJourneys = scheduledJourneys;
    }

    public Point getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(Point startingPoint) {
        this.startingPoint = startingPoint;
    }

    private Point startingPoint;
}
