package behaviours;

import agents.MachineAgent;
import agents.Process;
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

public class ReplyToRequestBehaviour extends CyclicBehaviour implements Loggable {
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        ACLMessage msg = myAgent.receive(mt);
        Process process = null;
        if (msg != null) {
            try {
                process = (Process) ((Message) msg.getContentObject()).getBody().get("process");
            } catch (UnreadableException e) {
                System.exit(1);
            }

            log(Level.WARNING, "[IN] [CFP] From: " + msg.getSender().getLocalName() + " | Process: " + process);
            ACLMessage reply = msg.createReply();

            if (((MachineAgent) myAgent).canPerform(process)) {
                reply.setPerformative(ACLMessage.PROPOSE);
                Proposal proposal = new Proposal(myAgent.getAID(), process, ((MachineAgent) myAgent).getEarliestTimeAvailable(), ((MachineAgent) myAgent).getDuration(process));

                Message contentObject = new Message();
                contentObject.append("proposal", proposal);
                Communication.setContentObject(contentObject, reply);

                log(Level.WARNING, "[OUT] [PROPOSE] " + proposal.out());

            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("not-available");

                log(Level.WARNING, "[OUT] [REFUSE] " + "Process " + process);
            }
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
