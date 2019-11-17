package agents;

import utils.Point;

class Task extends Job {

    private Process process;
    private Point location;

    Task(Proposal proposal) {
        super(proposal.getProduct(), proposal.getMachine(), proposal.getProductStartTime(),
                (proposal.getProductStartTime() + proposal.getDuration()));
        this.process = proposal.getProcess();
        this.location = proposal.getLocation();
    }

    Process getProcess() {
        return process;
    }

    public Point getLocation() {
        return location;
    }
}