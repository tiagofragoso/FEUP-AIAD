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

public class AgentMachine extends Agent {

    private ArrayList<Task> tasks;
    private PriorityQueue<AgentProduct> products;

    protected void setup() {

        tasks = new ArrayList<>();
        products = new PriorityQueue<>();

        System.out.println(this.getName());
        System.out.println("Hi");
    }

    private class ScheduleTask extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                
            }
        }
    }
}

