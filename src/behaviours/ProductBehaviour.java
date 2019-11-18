package behaviours;

import agents.Process;
import agents.ProductAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.Loggable;
import utils.LoggableAgent;

import java.util.ArrayList;
import java.util.logging.Level;

public class ProductBehaviour extends Behaviour implements Loggable {
    private Behaviour currentBehaviour;

    @Override
    public void action() {
        if (currentBehaviour != null && !currentBehaviour.done()) return;

        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("machine");
            template.addServices(sd);

            DFAgentDescription[] results = DFService.search(myAgent, template);

            ArrayList<AID> machines = new ArrayList<>();
            for (DFAgentDescription result : results) {
                machines.add(result.getName());
            }
            myAgent().setMachines(machines);

            template = new DFAgentDescription();
            sd = new ServiceDescription();
            sd.setType("robot");
            template.addServices(sd);

            results = DFService.search(myAgent, template);

            ArrayList<AID> robots = new ArrayList<>();
            for (DFAgentDescription result : results) {
                robots.add(result.getName());
            }
            myAgent().setRobots(robots);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        Process nextProcess = myAgent().getNextProcess();
        if (nextProcess != null && !myAgent().isDone()) {
            log(Level.SEVERE, "[START] CFP for " + nextProcess);
            currentBehaviour = new ScheduleJobBehaviour(nextProcess);
            myAgent.addBehaviour(currentBehaviour);
        } else if (nextProcess == null && !myAgent().isDone()) {
            log(Level.SEVERE, "[START] CFP for dropoff");
            currentBehaviour = ScheduleJobBehaviour.ScheduleDropoffBehaviour();
            myAgent.addBehaviour(currentBehaviour);
        }
    }

    @Override
    public boolean done() {
        return myAgent().isComplete();
    }

    private ProductAgent myAgent() {
        return (ProductAgent) myAgent;
    }

    @Override
    public void log(Level level, String msg) {
        ((LoggableAgent) myAgent).log(level, msg);
    }
}
