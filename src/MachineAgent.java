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
        if (completeProcesses.isEmpty()) {
            return 0;
        }
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

        System.out.println("Created " + this.getLocalName());
        System.out.print("Process list: ");
        for (Task task : availableProcesses) {
            System.out.print(task.getProcess().getCode());
        }
        System.out.println();

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

        addBehaviour(new ReplyToRequest());
        addBehaviour(new ScheduleTask());
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
                System.out.println(myAgent.getLocalName() + " received request for " + process +
                        " from " + msg.getSender().getLocalName());
                ACLMessage reply = msg.createReply();

                if (((MachineAgent)myAgent).availableProcess(process)) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    body.put("time", Integer.toString(((MachineAgent) myAgent).getLastTime()));
                    Communication.setBody(body, reply);
                    System.out.println(myAgent.getLocalName() + " sent message PROPOSE for process " +
                            process + " with time " + body.get("time") + " to " + msg.getSender().getLocalName());
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");

                    System.out.println(myAgent.getLocalName() + " sent message REFUSE for process " +
                            process + "to " + reply.getSender().getLocalName());
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
                            process + " with start time " + body.get("start") + " and end at " + body.get("end") +
                            " to " + msg.getSender().getLocalName());
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
}

