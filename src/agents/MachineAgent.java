package agents;

import behaviours.ReplyToRequestBehaviour;
import behaviours.ScheduleTaskBehaviour;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.Pair;

import java.util.ArrayList;

public class MachineAgent extends Agent {

    private ArrayList<Task> availableProcesses = new ArrayList<>();
    private ArrayList<Pair<Task, String>> completeProcesses = new ArrayList<>();

    public boolean availableProcess(String code) {
        for (Task task : availableProcesses) {
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
        return completeProcesses.get(completeProcesses.size() - 1).left.getEnd();
    }

    public int getDuration(String code) {
        for (Task process : availableProcesses) {
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
        completeProcesses.add(new Pair<>(new Task(code, duration, start, start + duration), nameProduct));
    }

    protected void setup() {

        System.out.println("Created " + this.getLocalName());
        System.out.print("agents.Process list: ");
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
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new ReplyToRequestBehaviour());
        addBehaviour(new ScheduleTaskBehaviour());
    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Machine " + getAID().getName() + " terminating.");
    }
}

