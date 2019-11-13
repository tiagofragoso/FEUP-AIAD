package agents;

import behaviours.ProductBehaviour;
import jade.core.AID;
import jade.core.Agent;
import utils.Pair;

import java.util.ArrayList;

public class ProductAgent extends Agent {
    private ArrayList<Pair<Process, Boolean>> processes = new ArrayList<>();
    private ArrayList<Task> scheduledProcesses = new ArrayList<>();
    private int priority;
    private ArrayList<AID> machines = new ArrayList<>();

    public ProductAgent(String[] processes, int priority) {
        for (String code : processes) {
            this.processes.add(
                    new Pair<>(new Process(code), false)
            );
        }
        this.priority = priority;
    }

    public ArrayList<Pair<Process, Boolean>> getProcesses() {
        return this.processes;
    }

    public void completeProcess(Process process) {
        for (Pair<Process, Boolean> pair : processes) {
            if (pair.left.equals(process)) {
                pair.right = true;
            }
        }
    }

    public void scheduleProcess(Process process, int start, int duration) {
        scheduledProcesses.add(new Task(process, duration, start, start + duration));
    }

    public int getEarliestTimeAvailable() {
        if (scheduledProcesses.isEmpty()) {
            return 0;
        } else {
            return scheduledProcesses.get(scheduledProcesses.size() - 1).getEndTime();
        }
    }

    public ArrayList<AID> getMachines() {
        return machines;
    }

    public void setMachines(ArrayList<AID> machines) {
        this.machines = machines;
    }

    protected void setup() {

        // Logging
        System.out.println("Created " + this.getLocalName());
        System.out.print("agents.Process list: ");
        for (Pair<Process, Boolean> process : processes) {
            System.out.print(process.left.getCode());
        }
        System.out.println();


        addBehaviour(new ProductBehaviour(this, 1000));
    }

    public boolean isComplete() {
        return this.processes.size() == this.scheduledProcesses.size();
    }

    public Process getNextProcess() {
        for (Pair<Process, Boolean> p : processes) {
            if (!p.right)
                return p.left;
        }
        return null;
    }
}
