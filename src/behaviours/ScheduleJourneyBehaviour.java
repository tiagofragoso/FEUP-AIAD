package behaviours;

import agents.JourneyProposal;
import agents.Proposal;
import agents.RobotAgent;
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
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        ACLMessage msg = myAgent.receive(mt);
        Proposal proposal = null;

        Message contentObject = new Message();

        if (msg != null) {
            try {
                proposal = ((Proposal) ((Message) msg.getContentObject()).getBody().get("proposal"));
            } catch (UnreadableException e) {
                System.exit(1);
            }

            ACLMessage reply = msg.createReply();

            JourneyProposal jp = proposal.getJourneyProposal();

            if (proposalWasAccepted(jp) && (jp.getProductStartTime() - jp.getPickupDuration()) >= myAgent().getEarliestAvailableTime()) {

                myAgent().scheduleJourney(proposal);

                reply.setPerformative(ACLMessage.INFORM);

                log(Level.WARNING, "[OUT] [INFORM] " + jp.out());

            } else {

                reply.setPerformative(ACLMessage.FAILURE);

                log(Level.WARNING, "[OUT] [FAILURE] " + jp.out());
            }

            contentObject.append("proposal", proposal);
            Communication.setContentObject(contentObject, reply);
            myAgent.send(reply);
        } else {
            block();
        }
    }

    private RobotAgent myAgent() {
        return (RobotAgent) myAgent;
    }

    private boolean proposalWasAccepted(JourneyProposal proposal) {
        return proposal.getProduct() != null && proposal.getProductStartTime() != null;
    }

    @Override
    public void log(Level level, String msg) {
        ((LoggableAgent) myAgent).log(level, msg);
    }

}
