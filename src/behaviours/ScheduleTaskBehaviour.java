package behaviours;

import agents.MachineAgent;
import communication.Communication;
import communication.Message;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;

public class ScheduleTaskBehaviour extends CyclicBehaviour {
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
        ACLMessage msg = myAgent.receive(mt);
        String process = null, name = null;
        HashMap<String, String> body = new HashMap<>();
        int startTime = 0;
        if (msg != null) {
            try {
                process = ((Message) msg.getContentObject()).getBody().get("process");
                startTime = Integer.parseInt(((Message) msg.getContentObject()).getBody().get("start"));
                name = ((Message) msg.getContentObject()).getBody().get("name");
            } catch (UnreadableException e) {
                System.exit(1);
            }
            ACLMessage reply = msg.createReply();

            if (startTime >= ((MachineAgent) myAgent).getLastTime()) {
                int duration = ((MachineAgent) myAgent).getDuration(process);
                ((MachineAgent) myAgent).addProcess(process, startTime, duration, name);

                body.put("start", Integer.toString(startTime));
                body.put("duration", Integer.toString(duration));
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                Communication.setBody(body, reply);

                System.out.println(myAgent.getLocalName() + " sent message ACCEPT_PROPOSAL for process " +
                        process + " with start time " + body.get("start") + " and end at " + (Integer.parseInt(body.get("start")) +
                        ((MachineAgent) myAgent).getDuration(process)) + " to " + msg.getSender().getLocalName());
            } else {
                body.put("newTime", Integer.toString(((MachineAgent) myAgent).getLastTime()));
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                Communication.setBody(body, reply);

                System.out.println(myAgent.getLocalName() + " sent message REJECT_PROPOSAL for process " +
                        process + " with new time " + body.get("newTime") + " to " + msg.getSender().getLocalName());
            }

            myAgent.send(reply);
        } else {
            block();
        }
    }
}