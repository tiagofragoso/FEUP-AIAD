package behaviours;

import agents.MachineAgent;
import communication.Communication;
import communication.Message;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;

public class ReplyToRequestBehaviour extends CyclicBehaviour {
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage msg = myAgent.receive(mt);
        String process = null;
        HashMap<String, String> body = new HashMap<>();
        if (msg != null) {
            try {
                process = ((Message) msg.getContentObject()).getBody().get("process");
            } catch (UnreadableException e) {
                System.exit(1);
            }
            System.out.println(myAgent.getLocalName() + " received request for " + process +
                    " from " + msg.getSender().getLocalName());
            ACLMessage reply = msg.createReply();

            if (((MachineAgent) myAgent).availableProcess(process)) {
                reply.setPerformative(ACLMessage.PROPOSE);
                body.put("time", Integer.toString(((MachineAgent) myAgent).getLastTime()));
                Communication.setBody(body, reply);
                System.out.println(myAgent.getLocalName() + " sent message PROPOSE for process " +
                        process + " with time " + body.get("time") + " to " + msg.getSender().getLocalName());
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("not-available");

                System.out.println(myAgent.getLocalName() + " sent message REFUSE for process " +
                        process + " to " + msg.getSender().getLocalName());
            }
            myAgent.send(reply);

        } else {
            block();
        }
    }
}
