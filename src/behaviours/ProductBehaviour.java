package behaviours;

import agents.ProductAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;

public class ProductBehaviour extends TickerBehaviour {
    public ProductBehaviour(Agent a, int milis) {
        super(a, milis);
    }

    @Override
    protected void onTick() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("machine");
        template.addServices(sd);
        try {
            DFAgentDescription[] results = DFService.search(myAgent, template);

            System.out.println("Search returns for " + myAgent.getAID().getName() + " : " + results.length + " elements");
            ArrayList<AID> machines = new ArrayList<>();
            for (DFAgentDescription result : results) {
                machines.add(result.getName());
            }
            ((ProductAgent) myAgent).setMachines(machines);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        System.out.println(((ProductAgent) myAgent).getProcesses().get(0).left.getCode());

        myAgent.addBehaviour(new MachineRequestBehaviour(((ProductAgent) myAgent).getProcesses().get(0).left.getCode()));

    }
}
