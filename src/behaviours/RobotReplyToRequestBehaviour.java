package behaviours;

import agents.*;
import agents.Process;
import communication.Communication;
import communication.Message;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.Loggable;
import utils.LoggableAgent;
import utils.Point;

import java.util.logging.Level;

public class RobotReplyToRequestBehaviour extends CyclicBehaviour implements Loggable {
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        ACLMessage msg = myAgent.receive(mt);
        Point pickupPoint = null;
        Point dropoffPoint = null;
        Proposal machineProposal = null;
        if (msg != null) {
            try {
                machineProposal = (Proposal) ((Message) msg.getContentObject()).getBody().get("machineProposal");
                pickupPoint = (Point) ((Message) msg.getContentObject()).getBody().get("pickupPoint");
                dropoffPoint = (Point) ((Message) msg.getContentObject()).getBody().get("dropoffPoint");
            } catch (UnreadableException e) {
                System.exit(1);
            }

            log(Level.WARNING, "[IN] [CFP] From: " + msg.getSender().getLocalName() + " | From " + pickupPoint + " to " + dropoffPoint);
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.PROPOSE);
            JourneyProposal proposal = generateProposal(pickupPoint, dropoffPoint);
            Message contentObject = new Message();
            machineProposal.setJourneyProposal(proposal);
            contentObject.append("proposal", machineProposal);
            Communication.setContentObject(contentObject, reply);
            log(Level.WARNING, "[OUT] [PROPOSE] " + proposal.out());
            myAgent.send(reply);
        } else {
            block();
        }
    }

    private JourneyProposal generateProposal(Point pickupPoint, Point dropoffPoint) {
        int pickupDuration = ((RobotAgent) myAgent).getPickupDuration(pickupPoint);
        int earliestAvailableTime =  ((RobotAgent) myAgent).getEarliestAvailableTime() + pickupDuration;
        return new JourneyProposal(myAgent.getAID(), earliestAvailableTime, pickupDuration,pickupPoint, dropoffPoint, ((RobotAgent) myAgent).getPickupDuration(pickupPoint));
    }

    @Override
    public void log(Level level, String msg) {
        ((LoggableAgent) myAgent).log(level, msg);
    }
}
