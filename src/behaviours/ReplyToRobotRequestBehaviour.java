package behaviours;

import agents.JourneyProposal;
import agents.RobotAgent;
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

public class ReplyToRobotRequestBehaviour extends CyclicBehaviour implements Loggable {
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        ACLMessage msg = myAgent.receive(mt);
        Point startPoint, endPoint;
        if (msg != null) {
            try {
                startPoint = (Point) ((Message) msg.getContentObject()).getBody().get("startPoint");
                endPoint = (Point) ((Message) msg.getContentObject()).getBody().get("endPoint");
            } catch (UnreadableException e) {
                e.printStackTrace();
                return;
            }

            log(Level.WARNING, "[IN] [CFP] From: " + msg.getSender().getLocalName() + " | Start point: " + startPoint + " | End point: " + endPoint);
            ACLMessage reply = msg.createReply();

            reply.setPerformative(ACLMessage.PROPOSE);
            int pickupTime = ((RobotAgent) myAgent).getEarliestTimeAvailable() + ((RobotAgent) myAgent).getVelocity() * ((RobotAgent) myAgent).getCurrentPoint().distanceTo(startPoint);
            int journeyDuration = startPoint.distanceTo(endPoint);
            JourneyProposal journeyProposal = new JourneyProposal(myAgent.getAID(), ((RobotAgent) myAgent).getEarliestTimeAvailable(),
                    pickupTime, journeyDuration, startPoint, endPoint, ((RobotAgent) myAgent).getCurrentPoint());

            Message contentObject = new Message();
            contentObject.append("proposal", journeyProposal);
            Communication.setContentObject(contentObject, reply);

            log(Level.WARNING, "[OUT] [PROPOSE] " + journeyProposal.out());

            myAgent.send(reply);
        } else {
            block();
        }
    }

    @Override
    public void log(Level level, String msg) {
        ((LoggableAgent) myAgent).log(level, msg);
    }
}
