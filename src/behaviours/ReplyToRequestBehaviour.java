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

public class ReplyToRequestBehaviour extends CyclicBehaviour {
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage msg = myAgent.receive(mt);
        Process process = null;
        if (msg != null) {
            try {
                process = (Process) ((Message) msg.getContentObject()).getBody().get("process");
            } catch (UnreadableException e) {
                System.exit(1);
            }
            System.out.println(myAgent.getLocalName() + " received request for " + process +
                    " from " + msg.getSender().getLocalName());
            ACLMessage reply = msg.createReply();

            if (((MachineAgent) myAgent).canPerform(process)) {
                reply.setPerformative(ACLMessage.PROPOSE);
                Proposal proposal = new Proposal(myAgent.getAID(), process, ((MachineAgent) myAgent).getEarliestTimeAvailable(), ((MachineAgent) myAgent).getDuration(process));

                Message contentObject = new Message();
                contentObject.append("proposal", proposal);
                Communication.setContentObject(contentObject, reply);

                System.out.println(myAgent.getLocalName() + " sent message PROPOSE for process " +
                        process + " with time " + proposal.getMachineEarliestAvailableTime() + " to " + msg.getSender().getLocalName());
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
