package behaviours;

import agents.MachineAgent;
import agents.Proposal;
import communication.Communication;
import communication.Message;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.Loggable;
import utils.LoggableAgent;

import java.util.logging.Level;

public class ScheduleTaskBehaviour extends CyclicBehaviour implements Loggable {
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        ACLMessage msg = myAgent.receive(mt);
        Proposal proposal = null;

        Message contentObject = new Message();

        if (msg != null) {
            try {
                proposal = (Proposal) ((Message) msg.getContentObject()).getBody().get("proposal");
            } catch (UnreadableException e) {
                System.exit(1);
            }

            ACLMessage reply = msg.createReply();


            if (proposalWasAccepted(proposal) &&
                    proposal.getProductStartTime() >= ((MachineAgent) myAgent).getEarliestTimeAvailable()) {

                ((MachineAgent) myAgent).scheduleTask(proposal);

                reply.setPerformative(ACLMessage.INFORM);

                log(Level.WARNING, "[OUT] [INFORM] " + proposal.out());

            } else {
                proposal.revokeAcceptance();
                proposal.setMachineEarliestAvailableTime(((MachineAgent) myAgent).getEarliestTimeAvailable());

                reply.setPerformative(ACLMessage.FAILURE);

                log(Level.WARNING, "[OUT] [FAILURE] " + proposal.out());
            }

            contentObject.append("proposal", proposal);
            Communication.setContentObject(contentObject, reply);
            myAgent.send(reply);
        } else {
            block();
        }
    }

    private boolean proposalWasAccepted(Proposal proposal) {
        return proposal.getProduct() != null && proposal.getProductStartTime() != null;
    }

    @Override
    public void log(Level level, String msg) {
        ((LoggableAgent) myAgent).log(level, msg);
    }
}