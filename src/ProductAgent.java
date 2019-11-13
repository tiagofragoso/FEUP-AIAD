import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.*;

public class ProductAgent extends Agent {
    private ArrayList<Pair<Process, Boolean>> processes = new ArrayList<>();
    private ArrayList<Task> completeProcessses = new ArrayList<>();
    private int priority;
    private AID[] machines;

    public ProductAgent(String[] processes, int priority) {
        for (String code : processes) {
            this.processes.add(
                new Pair<Process, Boolean>(new Process(code), false)
            );
        }
        this.priority = priority;
    }

    public ArrayList<Pair<Process, Boolean>> getProcesses() {
        return this.processes;
    }
    
    public void completeProcess (String code) {
        for (Pair<Process, Boolean> process : processes) {
            if (process.left.getCode().equals(code)) {
                process.right = true;
            }
        }
    }

    public void addCompleteProcess(String process, int start, int duration) {
        completeProcessses.add(new Task(process, duration, start, start+duration));
    }

    public int getLastTime() {
        if (completeProcessses.isEmpty()) {
            return 0;
        } else {
            return completeProcessses.get(completeProcessses.size()-1).getEnd();
        }
    }
    

    protected void setup() {
        System.out.println("Created " + this.getLocalName());
        System.out.print("Process list: ");
        for (Pair<Process,Boolean> process : processes) {
            System.out.print(process.getLeft().getCode());
        }
        System.out.println();
        addBehaviour(new TickerBehaviour(this, 1000){

             @Override
             protected void onTick() {
                 DFAgentDescription template = new DFAgentDescription();
                 ServiceDescription sd = new ServiceDescription();
                 sd.setType("machine");
                 template.addServices(sd);
                 try {
                     DFAgentDescription[] results = DFService.search( myAgent, template);

                     System.out.println("Search returns for " + myAgent.getAID().getName() + " : " + results.length + " elements" );
                     if (results.length>0)
                         machines = new AID[results.length];
                         for (int i = 0; i < results.length; ++i) {
                             machines[i] = results[i].getName();
                             System.out.println(" " + results[i].getName() );
                         }
                 } catch (FIPAException e) {
                     e.printStackTrace();
                 }

                 System.out.println(((ProductAgent)myAgent).getProcesses().get(0).left.getCode());
                 myAgent.addBehaviour(new MachineRequest(((ProductAgent)myAgent).getProcesses().get(0).left.getCode()));

             }
         });
    }

    private class MachineRequest extends Behaviour {

        private String process;
        private MessageTemplate mt;
        private int step = 0;
        private int replies = 0;
        private PriorityQueue<Pair<Integer, AID>> timeResponses = new PriorityQueue<Pair<Integer, AID>>(new TimeComparator());

        private int retryTime = 0;
        private AID retryMachine = null;
        private boolean retry = false;

        private MachineRequest(String process) {
            this.process = process;
        }

        private void removeBestMachine () {
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
            for (int i = 0; i < machines.length; ++i) {
                msg.addReceiver(machines[i]);
            }
            body.put("process", this.process);
            Communication.prepareMessage(body, msg, "process-request", "cfp"+System.currentTimeMillis());
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
                if (replies >= machines.length) {
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

            //TODO adicionar step
            if (retry) {
                bestTime = this.retryTime;
                bestMachine = this.retryMachine;
                retry = false;
            } else {
                removeBestMachine();
            }

            timeConfirmation.addReceiver(bestMachine);

            if (bestTime < lastTime) {
                body.put("start", Integer.toString(lastTime));
            } else {
                body.put("start", Integer.toString(bestTime));
            }
            body.put("process", this.process);
            body.put("name", myAgent.getName());

            Communication.prepareMessage(body, timeConfirmation, "process-request", "confirmation"+System.currentTimeMillis());
            myAgent.send(timeConfirmation);

            System.out.println(myAgent.getLocalName() + " sent message CONFIRMATION for process "
                    + this.process + " with start time " + body.get("start") + " to " + bestMachine.getLocalName());

            mt = Communication.prepareMessageTemplate(timeConfirmation, "process-request");
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
                            + " starting at " + startTime + " and with end at " + (startTime+duration));

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
                    } else {
                        System.out.println(myAgent.getLocalName() + " retrying with second best offer with time " + getBestTime());
                    }

                    step = 2;
                    return;
                }
                step = 4;
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
                step = 3;
                break;
            case 3:
                receiveConfirmation();
                break;
            }
        }

        public boolean done() {
            /**if (step == 2 && timeResponses.isEmpty()) {
                System.out.println("Attempt failed: " + this.process + " process not available.");
            }**/
            return ((step == 4));
        }

        class TimeComparator implements Comparator<Pair<Integer, AID>>{
            public int compare(Pair<Integer, AID> t1, Pair<Integer, AID> t2) {
                if (t1.left < t2.left)
                    return 1;
                else if (t1.left > t2.left)
                    return -1;
                return 0;
            }
        }

    }
}
