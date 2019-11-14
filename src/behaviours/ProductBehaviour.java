package behaviours;

import agents.Process;
import agents.ProductAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.Loggable;
import utils.LoggableAgent;

import java.util.ArrayList;
import java.util.logging.Level;

public class ProductBehaviour extends TickerBehaviour implements Loggable {
    private Behaviour currentBehaviour;

    public ProductBehaviour(Agent a, int milis) {
        super(a, milis);
    }

    @Override
    protected void onTick() {
        if (((ProductAgent) myAgent).isComplete()) {
            this.stop();
            return;
        }

        if (currentBehaviour != null && !currentBehaviour.done()) return;

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("machine");
        template.addServices(sd);
        try {
            DFAgentDescription[] results = DFService.search(myAgent, template);

            ArrayList<AID> machines = new ArrayList<>();
            for (DFAgentDescription result : results) {
                machines.add(result.getName());
            }
            ((ProductAgent) myAgent).setMachines(machines);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        Process nextProcess = ((ProductAgent) myAgent).getNextProcess();
        if (nextProcess != null) {
            log(Level.SEVERE, "[START] CFP for " + nextProcess);
            currentBehaviour = new MachineRequestBehaviour(nextProcess);
            myAgent.addBehaviour(currentBehaviour);
        }

    }

    @Override
    public void log(Level level, String msg) {
        ((LoggableAgent)myAgent).log(level, msg);
    }
}
