package agents;

import behaviours.ReplyToRequestBehaviour;
import behaviours.ScheduleTaskBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.LoggableAgent;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class MachineAgent extends LoggableAgent {

    private ArrayList<Task> availableProcesses = new ArrayList<>();
    private ArrayList<Pair<Task, String>> scheduledProcesses = new ArrayList<>();

    public MachineAgent() {
    }

    public MachineAgent(HashMap<String, Integer> processes) {
        processes.forEach((p, t) -> availableProcesses.add(new Task(new Process(p), t)));
    }

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
        this.bootstrapAgent(this);

        log(Level.SEVERE, "Created");

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Available processes: ");
        for (Task task : availableProcesses) {
            stringBuilder.append(task.getProcess().getCode());
        }

        log(Level.SEVERE, stringBuilder.toString());

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
        log(Level.SEVERE, "Terminating");
    }
}

