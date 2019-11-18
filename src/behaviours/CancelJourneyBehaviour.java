package behaviours;

import agents.Journey;
import agents.Proposal;
import agents.RobotAgent;
import communication.Message;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.Loggable;
import utils.LoggableAgent;

import java.util.logging.Level;

public class CancelJourneyBehaviour extends CyclicBehaviour implements Loggable {
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
        ACLMessage msg = myAgent.receive(mt);
        Proposal proposal;
        Integer startTime = null;
        if (msg != null) {
            try {
                proposal = (Proposal) ((Message) msg.getContentObject()).getBody().get("proposal");
                startTime = (new Journey(proposal)).getStartTime();
            } catch (UnreadableException e) {
                System.exit(1);
            }

            log(Level.SEVERE, "[IN] [CANCEL] From: " + msg.getSender().getLocalName() + " | Cancel journey at " + startTime);

            for (Journey j : myAgent().getScheduledJourneys()) {
                if (j.getStartTime() == startTime) {
                    myAgent().getScheduledJourneys().remove(j);
                    return;
                }
            }

        } else {
            block();
        }
    }

    private RobotAgent myAgent() {
        return (RobotAgent) myAgent;
    }

    @Override
    public void log(Level level, String msg) {
        ((LoggableAgent) myAgent).log(level, msg);
    }
}
