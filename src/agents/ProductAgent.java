package agents;

import behaviours.ProductBehaviour;
import jade.core.AID;
import utils.LoggableAgent;
import utils.Pair;
import utils.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class ProductAgent extends LoggableAgent {
    private LinkedHashMap<Process, Boolean> processes = new LinkedHashMap<>();
    private ArrayList<Task> scheduledTasks = new ArrayList<>();
    private int priority;
    private ArrayList<AID> machines = new ArrayList<>();

    public ProductAgent(String[] processes, int priority) {
        for (String code : processes) {
            this.processes.put(new Process(code), false);
        }
        this.priority = priority;
    }

    public LinkedHashMap<Process, Boolean> getProcesses() {
        return this.processes;
    }

    public void markProcessComplete(Process process) {
        processes.computeIfPresent(process, (p, v) -> true );
    }

    public void scheduleTask(Proposal proposal) {
        scheduledTasks.add(new Task(proposal));
    }

    public int getEarliestTimeAvailable() {
        if (scheduledTasks.isEmpty()) {
            return 0;
        } else {
            return scheduledTasks.get(scheduledTasks.size() - 1).getEndTime();
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
        processes.forEach((p, c) -> stringBuilder.append(p));

        log(Level.SEVERE, stringBuilder.toString());

        addBehaviour(new ProductBehaviour(this, 1000));
    }

    public boolean isComplete() {
        return this.processes.size() == this.scheduledTasks.size();
    }

    public Process getNextProcess() {
        for (Map.Entry<Process, Boolean> entry: processes.entrySet()) {
            if (!entry.getValue())
                return entry.getKey();
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
            for (Task t: this.scheduledTasks) {
                Object[] row = new Object[] {t.getStartTime()+"-"+t.getEndTime(), t.getProcess(), t.getWorkerName()};
                table.addRow(row);
            }
            table.print();
        }
    }
}
