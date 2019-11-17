package agents;

import jade.core.AID;
import jade.core.Agent;
import utils.Point;

import java.io.Serializable;
import java.util.Objects;

public class JourneyProposal implements Serializable {
    private AID robot;
    private int robotEarliestAvailableTime;
    private int duration;
    private int pickupDuration;
    private Point pickupPoint;
    private Point dropoffPoint;

    private AID product;
    private Integer productStartTime = null;

    public JourneyProposal(AID robot, int robotEarliestAvailableTime, int duration, Point pickupPoint, Point dropoffPoint, int pickupDuration) {
        this.robot = robot;
        this.robotEarliestAvailableTime = robotEarliestAvailableTime;
        this.duration = duration;
        this.pickupPoint = pickupPoint;
        this.dropoffPoint = dropoffPoint;
        this.pickupDuration = pickupDuration;
    }

    public AID getProduct() {
        return product;
    }

    public AID getRobot() {
        return robot;
    }

    public Integer getProductStartTime() {
        return productStartTime;
    }

    public int getDuration() {
        return duration;
    }

    public int getRobotEarliestAvailableTime() {
        return robotEarliestAvailableTime;
    }

    public String getProductName() { return product.getLocalName(); }

    public int getPickupDuration() {
        return pickupDuration;
    }

    private boolean isAccepted() {
        return this.product != null && this.productStartTime != null;
    }

    public void accept(AID product, int productStartTime) {
        this.product = product;
        this.productStartTime = productStartTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JourneyProposal that = (JourneyProposal) o;
        return robotEarliestAvailableTime == that.robotEarliestAvailableTime &&
                duration == that.duration &&
                pickupDuration == that.pickupDuration &&
                Objects.equals(robot, that.robot) &&
                Objects.equals(pickupPoint, that.pickupPoint) &&
                Objects.equals(dropoffPoint, that.dropoffPoint) &&
                Objects.equals(product, that.product) &&
                Objects.equals(productStartTime, that.productStartTime);
    }

    public Point getDropoffPoint() {
        return dropoffPoint;
    }

    public Point getPickupPoint() {
        return pickupPoint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(robot, robotEarliestAvailableTime, duration, pickupDuration, pickupPoint, dropoffPoint, product, productStartTime);
    }

    public String in() {
        return "Proposal from " + this.robot.getLocalName() + " " + this;
    }

    public String out() {
        return "Proposal " + this;
    }

    @Override
    public String toString() {
        return "for journey from " + this.pickupPoint + " to " + this.dropoffPoint  + ": Prop. Start: " + this.getRobotEarliestAvailableTime() + " | Duration: "
                + this.duration + (isAccepted() ? (" | Accepted by " + this.getProductName() + " | Start: " + this.productStartTime
                + " | End: " + (this.productStartTime + this.duration)) : "");
    }
}

