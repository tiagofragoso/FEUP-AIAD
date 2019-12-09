package agents;

import behaviours.ProposalBehaviour;
import behaviours.ScheduleTaskBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.LoggableAgent;
import utils.PlatformManager;
import utils.Point;
import utils.Table;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class MachineAgent extends LoggableAgent {

    private HashMap<Process, Integer> availableProcesses = new HashMap<>();
    private ArrayList<Task> scheduledTasks = new ArrayList<>();
    private Point location;

    public MachineAgent(Point location) {
        this.location = location;
    }

    public MachineAgent(HashMap<String, Integer> processes, Point location) {
        processes.forEach((p, t) -> availableProcesses.put(new Process(p), t));
        this.location = location;
    }

    public Point getLocation() {
        return location;
    }

    public boolean canPerform(Process process) {
        return availableProcesses.containsKey(process);
    }

    public int getEarliestTimeAvailable() {
        if (scheduledTasks.isEmpty()) {
            return 0;
        }
        return scheduledTasks.get(scheduledTasks.size() - 1).getEndTime();
    }

    public int getDuration(Process process) {
        return availableProcesses.getOrDefault(process, -1);
    }

    public void addAvailableProcess(String code, int duration) {
        availableProcesses.put(new Process(code), duration);
    }

    public void scheduleTask(Proposal proposal) {
        scheduledTasks.add(new Task(proposal));
    }

    protected void setup() {
        this.bootstrapAgent(this);

        log(Level.SEVERE, "Created");

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Available processes: ");
        availableProcesses.forEach((p, _d) -> stringBuilder.append(p));

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

        addBehaviour(new ProposalBehaviour());
        addBehaviour(new ScheduleTaskBehaviour());
    }

    protected void takeDown() {
        //printSchedule();
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        log(Level.SEVERE, "Terminating");
    }

    @Override
    public void printSchedule() {
        synchronized ( PlatformManager.getInstance().out()) {
            PrintWriter out = PlatformManager.getInstance().out();
            out.println("MACHINE: " + this.getLocalName());
            Table table = new Table(out, new String[]{"Time", "Process", "Product"}, 50);
            for (Task task : this.scheduledTasks) {
                Object[] row = new Object[]{task.getStartTime() + "-" + task.getEndTime(), task.getProcess(),
                        task.getProductName()};
                table.addRow(row);
            }
            table.print();
            out.flush();
        }
    }
}

