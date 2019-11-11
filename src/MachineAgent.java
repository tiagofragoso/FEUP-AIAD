import java.util.ArrayList;
import java.util.PriorityQueue;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import javax.swing.*;

public class MachineAgent extends Agent {

    private ArrayList<Task> tasks;
    private PriorityQueue<ProductAgent> products;

    protected void setup() {

        tasks = new ArrayList<>();
        products = new PriorityQueue<>();

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


        System.out.println(this.getName());
        System.out.println("Hi");

        addBehaviour(new Reply());
    }

    private class ScheduleTask extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                
            }
        }
    }

    private class Reply extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                String title = msg.getContent();
                //JOptionPane.showMessageDialog(null, "Machine Received: " + msg.getContent());
                System.out.println("Machine Received: " + msg.getContent());
            }
            else {
                block();
            }
        }
    }
}

