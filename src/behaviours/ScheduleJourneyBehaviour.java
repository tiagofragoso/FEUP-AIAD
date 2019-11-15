package behaviours;

import agents.*;
import communication.Communication;
import communication.Message;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.Loggable;
import utils.LoggableAgent;

import java.util.logging.Level;

public class ScheduleJourneyBehaviour extends CyclicBehaviour implements Loggable {
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        ACLMessage msg = myAgent.receive(mt);
        JourneyProposal proposal = null;

        Message contentObject = new Message();

        if (msg != null) {
            try {
                proposal = (JourneyProposal) ((Message) msg.getContentObject()).getBody().get("proposal");
            } catch (UnreadableException e) {
                e.printStackTrace();
                return;
            }

            ACLMessage reply = msg.createReply();

            if (proposalWasAccepted(proposal) &&
                    proposal.getProductStartTime() >= ((RobotAgent) myAgent).getEarliestTimeAvailable() &&
                    proposal.getStartPoint().equals(((RobotAgent) myAgent).getCurrentPoint())) {

                ((RobotAgent) myAgent).scheduleJourney(proposal);

                reply.setPerformative(ACLMessage.INFORM);
                log(Level.WARNING, "[OUT] [INFORM] " + proposal.out());
            } else {
                proposal.revokeAcceptance();
                proposal.setRobotStartTime(((RobotAgent) myAgent).getEarliestTimeAvailable());
                proposal.setStartPoint(((RobotAgent) myAgent).getCurrentPoint());
                proposal.setPickupTime(proposal.getRobotStartTime() + proposal.getStartPoint().distanceTo(proposal.getPickupMachine()));

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

    @Override
    public void log(Level level, String msg) {
        ((LoggableAgent) myAgent).log(level, msg);
    }

    private boolean proposalWasAccepted(JourneyProposal proposal) {
        return proposal.getProduct() != null && proposal.getProductStartTime() != null;
    }
}
