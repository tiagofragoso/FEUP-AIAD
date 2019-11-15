package agents;

class Task extends Job {

    private Process process;

    Task(Proposal proposal) {
        super(proposal.getProduct(), proposal.getMachine(), proposal.getProductStartTime(),
                (proposal.getProductStartTime()+ proposal.getDuration()));
        this.process = proposal.getProcess();
    }

    Process getProcess() {
        return process;
    }


}