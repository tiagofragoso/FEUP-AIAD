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
    private ArrayList<Pair<Task, String>> scheduledProcesses = new ArrayList<>();

    public boolean canPerform(Process process) {
        for (Task task : availableProcesses) {
            if (task.getProcess().equals(process)) {
                return true;
            }
        }

        return false;
    }

    public int getEarliestTimeAvailable() {
        if (scheduledProcesses.isEmpty()) {
            return 0;
        }
        return scheduledProcesses.get(scheduledProcesses.size() - 1).left.getEndTime();
    }

    public int getDuration(Process process) {
        for (Task task : availableProcesses) {
            if (task.getProcess().equals(process)) {
                return task.getDuration();
            }
        }
        return -1;
    }

    public void addTask(String code, int duration) {
        availableProcesses.add(new Task(new Process(code), duration));
    }

    public void addProcess(Process process, int start, int duration, String nameProduct) {
        scheduledProcesses.add(new Pair<>(new Task(process, duration, start, start + duration), nameProduct));
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

