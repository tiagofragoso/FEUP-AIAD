import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.UnreadableException;

import javax.swing.*;

public class MachineAgent extends Agent {

    private ArrayList<Task> availableProcesses = new ArrayList<>();
    private ArrayList<Pair<Task, String>> completeProcesses = new ArrayList<>();

    public boolean availableProcess (String code) {
        for (Task task: availableProcesses) {
            if (task.getProcess().getCode().equals(code)) {
                return true;
            }
        }

        return false;
    }

    public int getLastTime() {
        return completeProcesses.get(completeProcesses.size()-1).left.getEnd();
    }

    public int getDuration(String code) {
        for (Task process: availableProcesses) {
            if (process.getProcess().getCode().equals(code)) {
                return process.getDuration();
            }
        }
        return -1;
    }

    public void addTasK(String code, int duration) {
        availableProcesses.add(new Task(new Process(code), duration));
    }

    public void addProcess(String code, int start, int duration, String nameProduct) {
        completeProcesses.add(new Pair<>(new Task(code, duration, start, start+duration), nameProduct));
    }

    protected void setup() {

        // Register the machine service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("machine");
        sd.setName("JADE-machine");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }


        System.out.println("This is machine" + this.getName());
        addBehaviour(new ReplyToRequest());
    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Machine "+getAID().getName()+" terminating.");
    }


    private class ReplyToRequest extends CyclicBehaviour {
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
                System.out.println("Machine Received: request for " + process);
                ACLMessage reply = msg.createReply();

                if (((MachineAgent)myAgent).availableProcess(process)) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    body.put("time", Integer.toString(((MachineAgent) myAgent).getLastTime()));
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

    private class ScheduleTask extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
            ACLMessage msg = myAgent.receive(mt);
            String process = null, name = null;
            HashMap<String, String> body = new HashMap<>();
            int startTime = 0;
            if (msg != null) {
                try {
                    process = ((Message) msg.getContentObject()).getBody().get("process");
                    startTime = Integer.parseInt(((Message) msg.getContentObject()).getBody().get("time"));
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
                    try {
                        reply.setContentObject(
                                new Message(Message.message_type.SCHEDULED, body)
                        );
                    } catch (IOException e) {
                        System.out.println(e.getStackTrace());
                    }
                } else {
                    body.put("newTime", Integer.toString(((MachineAgent) myAgent).getLastTime()));
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    try {
                        reply.setContentObject(
                                new Message(Message.message_type.NOT_AVAILABLE, body)
                        );
                    } catch (IOException e) {
                        System.out.println(e.getStackTrace());
                    }
                }

                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}

