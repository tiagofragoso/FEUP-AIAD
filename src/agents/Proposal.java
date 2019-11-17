package agents;

import jade.core.AID;
import jade.core.Agent;
import utils.Point;

import java.io.Serializable;
import java.util.Objects;

public class Proposal implements Serializable {
    private AID machine;
    private Process process;
    private int machineEarliestAvailableTime;
    private int duration;
    private Point location;

    private AID product;
    private Integer productStartTime = null;
    private JourneyProposal journeyProposal;

    public Proposal(AID machine, Process process, int machineEarliestAvailableTime, int duration, Point location) {
        this.machine = machine;
        this.process = process;
        this.machineEarliestAvailableTime = machineEarliestAvailableTime;
        this.duration = duration;
        this.location = location;
    }

    public Point getLocation() {
        return location;
    }

    public AID getMachine() {
        return machine;
    }

    public Process getProcess() {
        return process;
    }

    public int getMachineEarliestAvailableTime() {
        return machineEarliestAvailableTime;
    }

    public void setMachineEarliestAvailableTime(int machineEarliestAvailableTime) {
        this.machineEarliestAvailableTime = machineEarliestAvailableTime;
    }

    public int getDuration() {
        return duration;
    }

    public Integer getProductStartTime() {
        return productStartTime;
    }

    public AID getProduct() {
        return product;
    }

    public String getProductName() { return product.getLocalName(); }

    private boolean isAccepted() {
        return this.product != null && this.productStartTime != null;
    }

    public void accept(AID product, int productStartTime) {
        this.product = product;
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
        Proposal proposal = (Proposal) o;
        return duration == proposal.duration &&
                productStartTime.equals(proposal.productStartTime) &&
                Objects.equals(machine, proposal.machine) &&
                Objects.equals(process, proposal.process) &&
                Objects.equals(product, proposal.product) &&
                Objects.equals(journeyProposal, proposal.journeyProposal);
    }

    public void setJourneyProposal(JourneyProposal journeyProposal) {
        this.journeyProposal = journeyProposal;
    }

    public JourneyProposal getJourneyProposal() {
        return journeyProposal;
    }

    public String in() {
        return "Proposal from " + this.machine.getLocalName() + " " + this;
    }

    public String out() {
        return "Proposal " + this;
    }

    @Override
    public String toString() {
        return "for " + this.process + ": Prop. Start: " + this.getMachineEarliestAvailableTime() + " | Duration: "
                + this.duration + (isAccepted() ? (" | Accepted by " + this.getProductName() + " | Start: " + this.productStartTime
                + " | End: " + (this.productStartTime + this.duration)) : "");
    }
}
