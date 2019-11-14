package behaviours;

import agents.Process;
import agents.ProductAgent;
import agents.Proposal;
import communication.Communication;
import communication.Message;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.Loggable;
import utils.LoggableAgent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Level;

class MachineRequestBehaviour extends Behaviour implements Loggable {

    private Process process;
    private MessageTemplate currentMessageTemplate;
    private request_state state = request_state.CALL_FOR_PROPOSALS;
    private int replies = 0;
    private PriorityQueue<Proposal> proposals = new PriorityQueue<>(new ProposalComparator());
    private Proposal currentProposal;

    MachineRequestBehaviour(Process process) {
        this.process = process;
    }

    private Proposal peekBestProposal() {
        return proposals.isEmpty() ? null : proposals.peek();
    }

    private Proposal popBestProposal() {
        return proposals.isEmpty() ? null : proposals.poll();
    }

    private void sendRequests() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        Message contentObject = new Message();

        ArrayList<AID> machines = ((ProductAgent) myAgent).getMachines();
        for (AID machine : machines) {
            msg.addReceiver(machine);
        }

        contentObject.append("process", this.process);

        Communication.prepareMessage(contentObject, msg, "process-request", "cfp" + System.currentTimeMillis());
        myAgent.send(msg);

        log(Level.WARNING, "[OUT] [CFP] Process " + this.process);

        currentMessageTemplate = Communication.prepareMessageTemplate(msg, "process-request");
        state = request_state.RECEIVE_PROPOSALS;
    }

    private void receiveProposals() {
        ACLMessage reply = myAgent.receive(currentMessageTemplate);

        if (reply != null) {
            if (reply.getPerformative() == ACLMessage.PROPOSE) {
                try {
                    Proposal proposal = (Proposal) ((Message) reply.getContentObject()).getBody().get("proposal");
                    proposals.add(proposal);

                    log(Level.WARNING, "[IN] [PROPOSE] " + proposal.in());

                } catch (UnreadableException e) {
                    System.exit(1);
                }
            }

            replies++;

            if (replies >= ((ProductAgent) myAgent).getMachines().size()) {
                state = request_state.ACCEPT_PROPOSAL;
            }
        } else {
            block();
        }
    }

    private void sendConfirmation() {
        ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
        if (currentProposal == null)
            currentProposal = popBestProposal();

        assert currentProposal != null;
        msg.addReceiver(currentProposal.getMachine());
        int startTime = Math.max(((ProductAgent) myAgent).getEarliestTimeAvailable(), currentProposal.getMachineEarliestAvailableTime());

        currentProposal.accept(myAgent.getLocalName(), startTime);

        Message contentObject = new Message();
        contentObject.append("proposal", currentProposal);

        Communication.prepareMessage(contentObject, msg, "process-request", "confirmation" + System.currentTimeMillis());
        myAgent.send(msg);

        log(Level.WARNING, "[OUT] [CONFIRM] " + currentProposal.in());

        currentMessageTemplate = Communication.prepareMessageTemplate(msg, "process-request");
        state = request_state.RECEIVE_CONFIRMATION;
    }

    private void receiveConfirmation() {
        ACLMessage reply = myAgent.receive(currentMessageTemplate);
        Proposal proposal = null;
        if (reply != null) {
            if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                try {
                    proposal = (Proposal) ((Message) reply.getContentObject()).getBody().get("proposal");

                } catch (UnreadableException e) {
                    System.exit(1);
                }

                log(Level.WARNING, "[IN] [ACCEPT] " + proposal.in());

                if (proposal.equals(currentProposal)) {
                    ((ProductAgent) myAgent).completeProcess(proposal.getProcess());
                    ((ProductAgent) myAgent).scheduleProcess(proposal.getProcess(), proposal.getProductStartTime(), proposal.getDuration());
                }

                log(Level.SEVERE, "[SCHEDULE] " + proposal.in());

            } else if (reply.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                Proposal newProposal = null;
                try {
                    newProposal = (Proposal) ((Message) reply.getContentObject()).getBody().get("proposal");
                } catch (UnreadableException e) {
                    System.exit(1);
                }

                log(Level.WARNING, "[IN] [REJECT] " + newProposal.in());

                if (proposals.isEmpty() || computeProposalTime(newProposal) < computeProposalTime(peekBestProposal())) {
                    currentProposal = newProposal;
                    log(Level.WARNING, "[RETRY] " + newProposal.in());
                } else {
                    currentProposal = null;
                    log(Level.WARNING, "[FAIL] Accepting next best proposal");
                }
                state = request_state.ACCEPT_PROPOSAL;
                return;
            }
            state = request_state.DONE;
        } else {
            block();
        }
    }

    public void action() {
        switch (state) {
            case CALL_FOR_PROPOSALS:
                sendRequests();
                break;
            case RECEIVE_PROPOSALS:
                receiveProposals();
                break;
            case ACCEPT_PROPOSAL:
                sendConfirmation();
                break;
            case RECEIVE_CONFIRMATION:
                receiveConfirmation();
                break;
            case DONE:
            default:
                break;
        }
    }

    public boolean done() {
        if (state == request_state.ACCEPT_PROPOSAL && proposals.isEmpty() && currentProposal == null) {
            log(Level.SEVERE, "[FAIL] No valid proposals");
            return true;
        }
        return (state == request_state.DONE);
    }

    private int computeProposalTime(Proposal proposal) {
        return Math.max(((ProductAgent) myAgent).getEarliestTimeAvailable(), proposal.getMachineEarliestAvailableTime()) + proposal.getDuration();
    }

    @Override
    public void log(Level level, String msg) {
        ((LoggableAgent)myAgent).log(level, msg);
    }

    private enum request_state {CALL_FOR_PROPOSALS, RECEIVE_PROPOSALS, ACCEPT_PROPOSAL, RECEIVE_CONFIRMATION, DONE}

    class ProposalComparator implements Comparator<Proposal> {
        public int compare(Proposal p1, Proposal p2) {
            int t1 = computeProposalTime(p1);
            int t2 = computeProposalTime(p2);
            if (t1 < t2)
                return -1;
            else if (t1 > t2)
                return 1;
            return 0;
        }
    }

}
