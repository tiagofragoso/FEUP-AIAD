package agents;

import jade.core.AID;

import java.io.Serializable;
import java.util.Objects;

public class Proposal implements Serializable {
    private AID machine;
    private Process process;
    private int machineEarliestAvailableTime;
    private int duration;

    private String productName;
    private Integer productStartTime = null;

    public Proposal(AID machine, Process process, int machineEarliestAvailableTime, int duration) {
        this.machine = machine;
        this.process = process;
        this.machineEarliestAvailableTime = machineEarliestAvailableTime;
        this.duration = duration;
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

    public String getProductName() {
        return productName;
    }

    public void accept(String productName, int productStartTime) {
        this.productName = productName;
        this.productStartTime = productStartTime;
    }

    public void revokeAcceptance() {
        this.productName = null;
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
                Objects.equals(productName, proposal.productName);
    }

}
