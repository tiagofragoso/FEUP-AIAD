package behaviours;

import agents.MachineAgent;
import agents.Proposal;
import communication.Communication;
import communication.Message;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ScheduleTaskBehaviour extends CyclicBehaviour {
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
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

                ((MachineAgent) myAgent).addProcess(proposal.getProcess(), proposal.getProductStartTime(), proposal.getDuration(), proposal.getProductName());

                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                System.out.println(myAgent.getLocalName() + " sent message ACCEPT_PROPOSAL for process " +
                        proposal.getProcess() + " with start time " + proposal.getProductStartTime() + " and end at " +
                        (proposal.getMachineEarliestAvailableTime() + proposal.getDuration()) +
                        " to " + msg.getSender().getLocalName());
            } else {
                proposal.revokeAcceptance();
                proposal.setMachineEarliestAvailableTime(((MachineAgent) myAgent).getEarliestTimeAvailable());

                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);

                System.out.println(myAgent.getLocalName() + " sent message REJECT_PROPOSAL for process " +
                        proposal.getProcess() + " with new time " + proposal.getMachineEarliestAvailableTime() +
                        " to " + msg.getSender().getLocalName());
            }

            contentObject.append("proposal", proposal);
            Communication.setContentObject(contentObject, reply);
            myAgent.send(reply);
        } else {
            block();
        }
    }

    private boolean proposalWasAccepted(Proposal proposal) {
        return proposal.getProductName() != null && proposal.getProductStartTime() != null;
    }
}