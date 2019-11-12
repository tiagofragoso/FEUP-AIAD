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
        addBehaviour(new TickerBehaviour(this, 10000){

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
        private int lastTime = 0;
        private int step = 0;
        private int replies = 0;
        private TreeMap<Integer, AID> timeResponses = new TreeMap<>(Collections.reverseOrder());
        private int bestTime = Integer.MAX_VALUE;
        private AID bestMachine = null;
        private Map.Entry<Integer, AID> bestResponse;

        private int retryTime = 0;
        private AID retryMachine = null;
        private boolean retry = false;

        private MachineRequest(String process) {
            this.process = process;
        }

        private void removeBestMachine (AID machine, int time) {
            if (!timeResponses.isEmpty()) {
                System.out.println("Removed " + machine.getLocalName());
                timeResponses.remove(time, machine);
            }
        }

        private int getBestTime() {
            return timeResponses.isEmpty() ? Integer.MAX_VALUE : timeResponses.firstKey();
        }

        private AID getBestMachine() {
            return timeResponses.isEmpty() ? null : timeResponses.firstEntry().getValue();
        }

        public void action() {
            lastTime = ((ProductAgent)myAgent).getLastTime();
            switch (step) {
            case 0:
                System.out.println("start step 0");
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                for (int i = 0; i < machines.length; ++i) {
                    msg.addReceiver(machines[i]);
                }
                HashMap<String, String> body = new HashMap<String, String>();
                body.put("process", this.process);
                try {
                    msg.setContentObject(
                        new Message(Message.message_type.REQUEST, body)
                    );
                } catch (IOException e) {
                    System.out.println(e.getStackTrace());
                }
                
                msg.setConversationId("process-request");
                msg.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                myAgent.send(msg);
    
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("process-request"),
                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
                step = 1;
                System.out.println("end step 0");
                break;
            case 1:
                System.out.println("start step 1");
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {

                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        try {
                            int newTime = Integer.parseInt(((Message) reply.getContentObject()).getBody().get("time"));
                            System.out.println(newTime);
                            timeResponses.put(newTime, reply.getSender());
                        } catch (UnreadableException e) {
                            System.exit(1);
                        }
                    }
                    replies++;
                    if (replies >= machines.length) {
                        System.out.println("end step 1 with " + timeResponses.size());
                        step = 2;
                    }
                } else {
                    block();
                }
                break;
            case 2:
                System.out.println("start step 2");
                ACLMessage timeConfirmation = new ACLMessage(ACLMessage.CONFIRM);
                body = new HashMap<>();
                bestResponse = timeResponses.firstEntry();
                if (bestResponse != null) {
                    bestTime = bestResponse.getKey();
                    bestMachine = bestResponse.getValue();
                }
                if (retry) {
                    bestTime = this.retryTime;
                    bestMachine = this.retryMachine;
                    retry = false;
                } else {
                    removeBestMachine(bestMachine, bestTime);
                }
                timeConfirmation.addReceiver(bestMachine);

                if (bestTime < lastTime) {
                    body.put("start", Integer.toString(lastTime));
                } else {
                    body.put("start", Integer.toString(bestTime));
                }
                body.put("process", this.process);
                body.put("name", myAgent.getName());
                try {
                    timeConfirmation.setContentObject(
                            new Message(Message.message_type.CONFIRMATION, body)
                    );
                } catch (IOException e) {
                    System.out.println(e.getStackTrace());
                }
                timeConfirmation.setConversationId("process-request");
                timeConfirmation.setReplyWith("confirmation"+System.currentTimeMillis());
                myAgent.send(timeConfirmation);

                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("process-request"),
                        MessageTemplate.MatchInReplyTo(timeConfirmation.getReplyWith()));

                step = 3;
                System.out.println("end step 2");
                break;
            case 3:
                System.out.println("start step 3");
                reply = myAgent.receive(mt);
                int startTime = 0, duration = 0;
                if (reply != null) {
                    if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        try {
                            startTime = Integer.parseInt(((Message) reply.getContentObject()).getBody().get("start"));
                            duration = Integer.parseInt(((Message) reply.getContentObject()).getBody().get("duration"));

                        } catch (UnreadableException e) {
                            System.exit(1);
                        }

                        System.out.println(myAgent.getName() + "scheduled " + this.process + "on machine " + reply.getSender().getName()
                        + "starting at " + startTime + "and with duration " + duration);
                        
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

                        System.out.println("Product" + myAgent.getLocalName() + "Best time " + getBestTime());

                        if (newTime < getBestTime()) {
                            retry = true;
                            retryTime = newTime;
                            retryMachine = reply.getSender();
                        }

                        step = 2;
                        break;
                    }
                    System.out.println("end step 3");
                    step = 4;
                } else {
                    block();
                }
                break;
            }
        }

        public boolean done() {
            /**if (step == 2 && timeResponses.isEmpty()) {
                System.out.println("Attempt failed: " + this.process + " process not available.");
            }**/
            return ((step == 4));
        }

    }
}
