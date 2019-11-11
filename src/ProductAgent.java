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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ProductAgent extends Agent {
    private ArrayList<Pair<Process, Boolean>> processes = new ArrayList<>();
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
    

    protected void setup() {
        System.out.println("Created " + this.getName());
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
                 
                 myAgent.addBehaviour(new MachineRequest(((ProductAgent)myAgent).getProcesses().get(0).left.getCode()));
             }
         });
    }

    private class MachineRequest extends OneShotBehaviour {

        private String task;

        private MachineRequest(String task) {
            this.task = task;
        }

        public void action() {
            // Send the cfp to all sellers
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            for (int i = 0; i < machines.length; ++i) {
                msg.addReceiver(machines[i]);
            }
            HashMap<String, String> body = new HashMap<String, String>();
            body.put("task", this.task);
            try {
                msg.setContentObject(
                    new Message(Message.message_type.REQUEST, body)
                );
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
            }
            
            msg.setConversationId("task-request");
            //msg.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
            myAgent.send(msg);
    }

    }
}
