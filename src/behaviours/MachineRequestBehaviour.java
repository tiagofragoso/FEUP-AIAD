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
import utils.Pair;
import utils.Point;

import java.io.Serializable;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.logging.Level;

class MachineRequestBehaviour extends Behaviour implements Loggable {

    private Process process;
    private MessageTemplate currentMessageTemplate;
    private request_state state = request_state.CFP_MACHINES;
    private int machineReplies = 0;
    private int robotReplies = 0;
    private ArrayList<Proposal> machineProposals = new ArrayList<>();
    private PriorityQueue<Proposal> robotProposals = new PriorityQueue<>(new ProposalComparator());
    private Proposal acceptedProposal;

    MachineRequestBehaviour(Process process) {
        this.process = process;
    }

    private ProductAgent myAgent() { return (ProductAgent) myAgent; }

    private Proposal getBestProposal() {
        return robotProposals.isEmpty() ? null : robotProposals.peek();
    }


    private void sendCFP(ArrayList<AID> receivers, Pair<String, Object> ...content) {
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        receivers.forEach(msg::addReceiver);

        Message contentObject = new Message();
        for (Pair<String, Object> p: content) contentObject.append(p.left, p.right);

        Communication.prepareMessage(contentObject, msg, "process-request", "cfp" + System.currentTimeMillis());
        myAgent.send(msg);
        currentMessageTemplate = Communication.prepareMessageTemplate(msg, "process-request");

    }

    private void sendCFPtoMachines() {
        sendCFP(myAgent().getMachines(), new Pair<>("process", this.process));
        log(Level.WARNING, "[OUT] [CFP] Process " + this.process);
        state = request_state.PROPOSE_MACHINES;
    }

    private void sendCFPtoRobots() {
        for (Proposal p: machineProposals) {
            Point pickupPoint = myAgent().getLatestPickupPoint();
            Point dropoffPoint = p.getLocation();

            sendCFP(myAgent().getRobots(), new Pair<>("pickupPoint", pickupPoint), new Pair<>("dropoffPoint", dropoffPoint), new Pair<>("machineProposal", p));

            log(Level.WARNING, "[OUT] [CFP] Pickup at: " + pickupPoint + " | Dropoff point: " + dropoffPoint);
        }
        state = request_state.PROPOSE_ROBOTS;
    }

    private void receiveProposals(Consumer<ACLMessage> action) {
        ACLMessage reply = myAgent.receive(currentMessageTemplate);

        if (reply != null) {
            action.accept(reply);
        } else {
            block();
        }
    }

    private void receiveMachineProposals() {
        receiveProposals((message)  -> {
            if (message.getPerformative() == ACLMessage.PROPOSE) {
                try {
                    Proposal proposal = (Proposal) ((Message) message.getContentObject()).getBody().get("proposal");

                    this.machineProposals.add(proposal);

                    log(Level.WARNING, "[IN] [PROPOSE] " + proposal.in());

                } catch (Exception e) {
                    log(Level.SEVERE, e.getMessage());
                    state = request_state.DONE;
                }

            }
            machineReplies++;
            if (machineReplies >= myAgent().getMachines().size()) {
                state = request_state.CFP_ROBOTS;
            }
        });
    }

    private void receiveRobotProposals() {
        receiveProposals((message) -> {
            if (message.getPerformative() == ACLMessage.PROPOSE) {
                try {
                    Proposal proposal = (Proposal) ((Message) message.getContentObject()).getBody().get("proposal");

                    this.robotProposals.add(proposal);

                    log(Level.WARNING, "[IN] [PROPOSE] " + proposal.getJourneyProposal().in());

                } catch (Exception e) {
                    log(Level.SEVERE, e.getMessage());
                    state = request_state.DONE;
                }
            }
            robotReplies++;
            log(Level.SEVERE, "replies: " + robotReplies + " total: " + myAgent().getRobots().size()*machineProposals.size());
            if (robotReplies >= myAgent().getRobots().size()*machineProposals.size()) {
                state = request_state.ACCEPT_ROBOTS;
            }
        });
    }



    private void sendAccept(AID receiver, Pair<String, Object> content) {
        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        msg.addReceiver(receiver);

        Message contentObject = new Message();
        contentObject.append(content.left, content.right);

        Communication.prepareMessage(contentObject, msg, "process-request", "confirmation" + System.currentTimeMillis());
        myAgent.send(msg);
        currentMessageTemplate = Communication.prepareMessageTemplate(msg, "process-request");
    }

    private void acceptRobots() {
        Proposal proposal = getBestProposal();
        assert proposal != null;
        int startTime = Math.max(
                Math.max(myAgent().getEarliestTimeAvailable(), proposal.getJourneyProposal().getRobotEarliestAvailableTime()),
                (proposal.getMachineEarliestAvailableTime() - proposal.getJourneyProposal().getDuration())
        );

        proposal.getJourneyProposal().accept(myAgent.getAID(), startTime);
        sendAccept(proposal.getJourneyProposal().getRobot(), new Pair<>("proposal", proposal));
        acceptedProposal = proposal;

        log(Level.WARNING, "[OUT] [ACCEPT] " + proposal.getJourneyProposal().in());
        state = request_state.INFORM_ROBOTS;
    }

    private void acceptMachines() {
        int startTime = acceptedProposal.getJourneyProposal().getProductStartTime() + acceptedProposal.getJourneyProposal().getDuration();
        acceptedProposal.accept(myAgent.getAID(), startTime);

        sendAccept(acceptedProposal.getMachine(), new Pair<>("proposal", acceptedProposal));

        log(Level.WARNING, "[OUT] [ACCEPT] " + acceptedProposal.in());
        state = request_state.INFORM_MACHINES;
    }

    private void receiveConfirmation(Consumer<ACLMessage> action) {
        ACLMessage reply = myAgent.receive(currentMessageTemplate);
        if (reply != null) {
            action.accept(reply);
        } else {
            block();
        }
    }

    private void receiveRobotConfirmation() {
        receiveConfirmation((message) -> {
            if (message.getPerformative() == ACLMessage.INFORM) {
                Proposal proposal = null;
                try {
                    proposal = (Proposal) ((Message) message.getContentObject()).getBody().get("proposal");

                } catch (UnreadableException e) {
                    System.exit(1);
                }

                log(Level.WARNING, "[IN] [INFORM] " + proposal.getJourneyProposal().in());

                log(Level.SEVERE, "[SCHEDULE] " + proposal.getJourneyProposal().in());
                state = request_state.ACCEPT_MACHINES;
            } else if (message.getPerformative() == ACLMessage.FAILURE) {
                state = request_state.DONE;
            }
        });
    }

    private void receiveMachineConfirmation() {
        receiveConfirmation((message) -> {
            if (message.getPerformative() == ACLMessage.INFORM) {
                Proposal proposal = null;
                try {
                    proposal = (Proposal) ((Message) message.getContentObject()).getBody().get("proposal");

                } catch (UnreadableException e) {
                    System.exit(1);
                }

                log(Level.WARNING, "[IN] [INFORM] " + proposal.in());

                log(Level.SEVERE, "[SCHEDULE] " + proposal.in());

                myAgent().markProcessComplete(proposal.getProcess());
                myAgent().scheduleJob(proposal);

                state = request_state.DONE;
            } else if (message.getPerformative() == ACLMessage.FAILURE) {
                // cancel robot
                state = request_state.DONE;
            }
        });
    }

    public void action() {
        log(Level.SEVERE, "State " + state.toString());
        switch (state) {
            case CFP_MACHINES:
                sendCFPtoMachines();
                break;
            case PROPOSE_MACHINES:
                receiveMachineProposals();
                break;
            case CFP_ROBOTS:
                sendCFPtoRobots();
                break;
            case PROPOSE_ROBOTS:
                receiveRobotProposals();
                break;
            case ACCEPT_ROBOTS:
                acceptRobots();
                break;
            case INFORM_ROBOTS:
                receiveRobotConfirmation();
                break;
            case ACCEPT_MACHINES:
                acceptMachines();
                break;
            case INFORM_MACHINES:
                receiveMachineConfirmation();
                break;
            case DONE:
            default:
                break;
        }
    }

    public boolean done() {
        if ((state == request_state.CFP_ROBOTS && machineProposals.isEmpty()) ||
            (state == request_state.ACCEPT_ROBOTS && robotProposals.isEmpty())) {
            log(Level.SEVERE, "[FAIL] No valid machineProposals");
            return true;
        }
        return (state == request_state.DONE);
    }

    private int computeProposalTime(Proposal proposal) {
        return Math.max(
                Math.max(myAgent().getEarliestTimeAvailable(), proposal.getJourneyProposal().getRobotEarliestAvailableTime()),
                (proposal.getMachineEarliestAvailableTime() - proposal.getJourneyProposal().getDuration())
        ) + proposal.getJourneyProposal().getDuration() + proposal.getDuration();
    }

    @Override
    public void log(Level level, String msg) {
        ((LoggableAgent) myAgent).log(level, msg);
    }

    private enum request_state {
        CFP_MACHINES,
        PROPOSE_MACHINES,
        CFP_ROBOTS,
        PROPOSE_ROBOTS,
        ACCEPT_ROBOTS,
        INFORM_ROBOTS,
        ACCEPT_MACHINES,
        INFORM_MACHINES,
        DONE}

    private class ProposalComparator implements Comparator<Proposal>, Serializable {
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
