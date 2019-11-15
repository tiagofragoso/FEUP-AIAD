package agents;

import jade.core.AID;
import utils.Point;

import java.io.Serializable;
import java.util.Objects;

public class JourneyProposal implements Serializable {
    private AID robot;

    public void setRobotStartTime(int robotStartTime) {
        this.robotStartTime = robotStartTime;
    }

    public void setPickupTime(int pickupTime) {
        this.pickupTime = pickupTime;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    private int robotStartTime;
    private int pickupTime;
    private int journeyDuration;
    private Point startPoint;
    private Point pickupMachine;
    private Point dropoffMachine;

    private AID product;
    private Integer productStartTime;

    public JourneyProposal(AID robot, int robotStartTime, int pickupTime, int journeyDuration, Point pickupMachine, Point dropoffMachine, Point startPoint) {
        this.robot = robot;
        this.robotStartTime = robotStartTime;
        this.pickupTime = pickupTime;
        this.journeyDuration = journeyDuration;
        this.pickupMachine = pickupMachine;
        this.dropoffMachine = dropoffMachine;
        this.startPoint = startPoint;
    }

    public String getProductName() { return product.getLocalName(); }

    public AID getProduct() {
        return product;
    }

    public AID getRobot() {
        return robot;
    }

    public int getRobotStartTime() {
        return robotStartTime;
    }

    public int getJourneyDuration() {
        return journeyDuration;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getPickupMachine() {
        return pickupMachine;
    }

    public Point getDropoffMachine() {
        return dropoffMachine;
    }

    public Integer getProductStartTime() {
        return productStartTime;
    }

    private boolean isAccepted() {
        return this.product != null && this.productStartTime != null;
    }

    public void accept(AID productName, int productStartTime) {
        this.product = productName;
        this.productStartTime = productStartTime;
    }

    public void revokeAcceptance() {
        this.product = null;
        this.productStartTime = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JourneyProposal proposal = (JourneyProposal) o;
        return journeyDuration == proposal.journeyDuration &&
                productStartTime.equals(proposal.productStartTime) &&
                Objects.equals(robot, proposal.robot) &&
                Objects.equals(product, proposal.product) &&
                Objects.equals(pickupMachine, proposal.pickupMachine) &&
                Objects.equals(dropoffMachine, proposal.dropoffMachine) &&
                Objects.equals(startPoint, proposal.startPoint);
    }

    public String in() {
        return "Proposal from " + this.robot.getLocalName() + " " + this;
    }

    public String out() {
        return "Proposal " + this;
    }

    @Override
    public String toString() {
        return "from " + this.pickupMachine + " to " + this.dropoffMachine
                + ": Journey start: " + this.pickupTime + " | Duration: "
                + this.journeyDuration + (isAccepted() ? (" | Accepted by " + this.getProductName() + " | Start: " + this.productStartTime
                + " | End: " + (this.productStartTime + this.journeyDuration)) : "");
    }
}
