package behaviours;

import agents.ProductAgent;
import communication.Communication;
import communication.Message;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

class MachineRequestBehaviour extends Behaviour {

    private String process;
    private MessageTemplate mt;
    private int step = 0;
    private int replies = 0;
    private PriorityQueue<Pair<Integer, AID>> timeResponses = new PriorityQueue<>(new TimeComparator());

    private int retryTime = 0;
    private AID retryMachine = null;
    private boolean retry = false;

    MachineRequestBehaviour(String process) {
        this.process = process;
    }

    private void removeBestMachine() {
        if (!timeResponses.isEmpty()) {
            timeResponses.poll();
        }
    }

    private int getBestTime() {
        return timeResponses.isEmpty() ? Integer.MAX_VALUE : timeResponses.peek().left;
    }

    private AID getBestMachine() {
        return timeResponses.isEmpty() ? null : timeResponses.peek().right;
    }

    private void sendRequests() {
        HashMap<String, String> body = new HashMap<String, String>();
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        ArrayList<AID> machines = ((ProductAgent) myAgent).getMachines();
        for (int i = 0; i < machines.size(); ++i) {
            msg.addReceiver(machines.get(i));
        }
        body.put("process", this.process);
        Communication.prepareMessage(body, msg, "process-request", "cfp" + System.currentTimeMillis());
        myAgent.send(msg);

        System.out.println("Product " + myAgent.getLocalName() + " sent message INFORM for process " + this.process);

        mt = Communication.prepareMessageTemplate(msg, "process-request");
        step = 1;
    }

    private void receiveProposals() {
        ACLMessage reply = myAgent.receive(mt);
        if (reply != null) {
            if (reply.getPerformative() == ACLMessage.PROPOSE) {
                try {
                    int newTime = Integer.parseInt(((Message) reply.getContentObject()).getBody().get("time"));
                    timeResponses.add(new Pair<>(newTime, reply.getSender()));
                    System.out.println(myAgent.getLocalName() + " received message PROPOSE with time " + newTime +
                            " from " + reply.getSender().getLocalName());
                } catch (UnreadableException e) {
                    System.exit(1);
                }
            }
            replies++;
            if (replies >= ((ProductAgent) myAgent).getMachines().size()) {
                step = 2;
            }
        } else {
            block();
        }
    }

    private void sendConfirmation() {
        HashMap<String, String> body = new HashMap<String, String>();
        int lastTime = ((ProductAgent) myAgent).getLastTime();
        ACLMessage timeConfirmation = new ACLMessage(ACLMessage.CONFIRM);
        int bestTime = getBestTime();
        AID bestMachine = getBestMachine();

        removeBestMachine();

        timeConfirmation.addReceiver(bestMachine);

        if (bestTime < lastTime) {
            body.put("start", Integer.toString(lastTime));
        } else {
            body.put("start", Integer.toString(bestTime));
        }
        body.put("process", this.process);
        body.put("name", myAgent.getName());

        Communication.prepareMessage(body, timeConfirmation, "process-request", "confirmation" + System.currentTimeMillis());
        myAgent.send(timeConfirmation);

        System.out.println(myAgent.getLocalName() + " sent message CONFIRMATION for process "
                + this.process + " with start time " + body.get("start") + " to " + bestMachine.getLocalName());

        mt = Communication.prepareMessageTemplate(timeConfirmation, "process-request");
        step = 3;
    }

    private void retryConfirmation() {
        ACLMessage timeConfirmation = new ACLMessage(ACLMessage.CONFIRM);
        HashMap<String, String> body = new HashMap<String, String>();
        timeConfirmation.addReceiver(retryMachine);
        body.put("start", Integer.toString(retryTime));
        body.put("process", this.process);
        body.put("name", myAgent.getName());
        Communication.prepareMessage(body, timeConfirmation, "process-request", "confirmation" + System.currentTimeMillis());
        myAgent.send(timeConfirmation);
        System.out.println(myAgent.getLocalName() + " sent message CONFIRMATION for process "
                + this.process + " with start time " + body.get("start") + " to " + retryMachine.getLocalName());

        mt = Communication.prepareMessageTemplate(timeConfirmation, "process-request");
        step = 3;
    }

    private void receiveConfirmation() {
        ACLMessage reply = myAgent.receive(mt);
        int startTime = 0, duration = 0;
        if (reply != null) {
            if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                try {
                    startTime = Integer.parseInt(((Message) reply.getContentObject()).getBody().get("start"));
                    duration = Integer.parseInt(((Message) reply.getContentObject()).getBody().get("duration"));

                } catch (UnreadableException e) {
                    System.exit(1);
                }

                System.out.println(myAgent.getLocalName() + " received message ACCEPT_PROPOSAL for process " +
                        this.process + " from " + reply.getSender().getLocalName());

                System.out.println(myAgent.getLocalName() + " scheduled " + this.process + " on " + reply.getSender().getLocalName()
                        + " starting at " + startTime + " and with end at " + (startTime + duration));

                ((ProductAgent) myAgent).completeProcess(this.process);
                ((ProductAgent) myAgent).addCompleteProcess(this.process, startTime, duration);
                myAgent.doDelete();

            } else if (reply.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                int newTime = 0;
                try {
                    newTime = Integer.parseInt(((Message) reply.getContentObject()).getBody().get("newTime"));
                } catch (UnreadableException e) {
                    System.exit(1);
                }

                System.out.println(myAgent.getLocalName() + " received message REFUSE_PROPOSAL for process " +
                        this.process + " from " + reply.getSender().getLocalName() + " with new time " + newTime);

                if (newTime < getBestTime()) {
                    retry = true;
                    retryTime = newTime;
                    retryMachine = reply.getSender();
                    System.out.println(myAgent.getLocalName() + " retrying with new offer from " + retryMachine.getLocalName());
                    step = 4;
                } else {
                    System.out.println(myAgent.getLocalName() + " retrying with second best offer with time " + getBestTime());
                    step = 2;
                }

                return;
            }
            step = 5;
        } else {
            block();
        }
    }

    public void action() {
        switch (step) {
            case 0:
                sendRequests();
                break;
            case 1:
                receiveProposals();
                break;
            case 2:
                sendConfirmation();
                break;
            case 3:
                receiveConfirmation();
                break;
            case 4:
                retryConfirmation();
                break;
        }
    }

    public boolean done() {
        if (step == 2 && timeResponses.isEmpty()) {
            System.out.println("Attempt failed: " + this.process + " process not available.");
            return true;
        }
        return (step == 5);
    }

    class TimeComparator implements Comparator<Pair<Integer, AID>> {
        public int compare(Pair<Integer, AID> t1, Pair<Integer, AID> t2) {
            if (t1.left < t2.left)
                return 1;
            else if (t1.left > t2.left)
                return -1;
            return 0;
        }
    }

}
