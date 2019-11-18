package agents;

import behaviours.ProductBehaviour;
import jade.core.AID;
import utils.LoggableAgent;
import utils.PlatformManager;
import utils.Point;
import utils.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class ProductAgent extends LoggableAgent {
    private LinkedHashMap<Process, Boolean> processes = new LinkedHashMap<>();
    private ArrayList<Job> scheduledJobs = new ArrayList<>();
    private ArrayList<AID> machines = new ArrayList<>();
    private ArrayList<AID> robots = new ArrayList<>();
    private Point startingPoint;
    private Point dropoffPoint;
    private boolean done = false;

    public ProductAgent(String[] processes, Point startingPoint, Point dropoffPoint) {
        for (String code : processes) {
            this.processes.put(new Process(code), false);
        }
        this.startingPoint = startingPoint;
        this.dropoffPoint = dropoffPoint;
    }

    public LinkedHashMap<Process, Boolean> getProcesses() {
        return this.processes;
    }

    public Point getDropoffPoint() {
        return dropoffPoint;
    }

    public void markProcessComplete(Process process) {
        processes.computeIfPresent(process, (p, v) -> true);
    }

    public void scheduleJob(Proposal proposal) {
        scheduledJobs.add(new Journey(proposal));
        scheduledJobs.add(new Task(proposal));
    }

    public int getEarliestTimeAvailable() {
        if (scheduledJobs.isEmpty()) {
            return 0;
        } else {
            return scheduledJobs.get(scheduledJobs.size() - 1).getEndTime();
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

        addBehaviour(new ProductBehaviour(this));
    }

    public boolean isComplete() {
        return getNextProcess() == null && done;
    }

    public Process getNextProcess() {
        for (Map.Entry<Process, Boolean> entry : processes.entrySet()) {
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
            Table table = new Table(new String[]{"Time", "Job", "Worker"});
            for (Job j : this.scheduledJobs) {
                Object[] row = null;
                if (j instanceof Task) {
                    Task t = (Task) j;
                    row = new Object[]{t.getStartTime() + "-" + t.getEndTime(), "Process " + t.getProcess(), t.getWorkerName()};
                } else if (j instanceof Journey) {
                    Journey jo = (Journey) j;
                    row = new Object[]{jo.getStartTime() + "-" + jo.getEndTime(), jo.getPickupPoint() + " -> " + jo.getDropoffPoint(), jo.getWorkerName()};
                }
                table.addRow(row);
            }
            table.print();
        }
    }

    public Point getLatestPickupPoint() {
        if (scheduledJobs.isEmpty()) {
            return startingPoint;
        } else {
            Job job = scheduledJobs.get(scheduledJobs.size() - 1);
            if (job instanceof Task) {
                Task t = (Task) job;
                return t.getLocation();
            } else if (job instanceof Journey) {
                Journey j = (Journey) job;
                return j.getDropoffPoint();
            }
        }
        return null;
    }

    public ArrayList<AID> getRobots() {
        return robots;
    }

    public void setRobots(ArrayList<AID> robots) {
        this.robots = robots;
    }

    public void scheduleDropoff(Proposal proposal) {
        scheduledJobs.add(new Journey(proposal));
        done = true;
        log(Level.SEVERE, "Finished at " + this.getEarliestTimeAvailable());
        doDelete();
    }

    @Override
    protected void takeDown() {
        log(Level.SEVERE, "Terminating");
        PlatformManager.getInstance().registerProductTime(this.getEarliestTimeAvailable());
    }

    public boolean isDone() {
        return done;
    }
}
