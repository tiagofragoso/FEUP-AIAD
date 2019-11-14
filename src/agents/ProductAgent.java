package agents;

import behaviours.ProductBehaviour;
import jade.core.AID;
import utils.LoggableAgent;
import utils.Pair;
import utils.Table;

import java.util.ArrayList;
import java.util.logging.Level;

public class ProductAgent extends LoggableAgent {
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

        addShutdownHook();

        this.bootstrapAgent(this);

        // Logging
        log(Level.SEVERE, "Created");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Process sequence: ");
        for (Pair<Process, Boolean> process : processes) {
            stringBuilder.append(process.left.getCode());
        }

        log(Level.SEVERE, stringBuilder.toString());

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

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::printSchedule));
    }

    @Override
    public void printSchedule() {
        synchronized (System.out) {
            System.out.println("PRODUCT: " + this.getLocalName());
            Table table = new Table(new String[]{"Time", "Process", "Machine"}, 15);
            for (Task t: this.scheduledProcesses) {
                Object[] row = new Object[] {t.getStartTime()+"-"+t.getEndTime(), t.getProcess(), "Machine X"};
                table.addRow(row);
            }
            table.print();
        }
    }
}
