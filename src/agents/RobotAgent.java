package agents;

import behaviours.CancelJourneyBehaviour;
import behaviours.JourneyProposalBehaviour;
import behaviours.ScheduleJourneyBehaviour;
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
import java.util.logging.Level;

public class RobotAgent extends LoggableAgent {

    private int velocity;
    private ArrayList<Journey> scheduledJourneys = new ArrayList<>();
    private Point startingPoint;

    public RobotAgent(int velocity, Point startingPoint) {
        this.velocity = velocity;
        this.startingPoint = startingPoint;
    }

    protected void setup() {
        this.bootstrapAgent(this);

        log(Level.SEVERE, "Created");

        // Register the machine service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("robot");
        sd.setName("JADE-robot");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new JourneyProposalBehaviour());
        addBehaviour(new ScheduleJourneyBehaviour());
        addBehaviour(new CancelJourneyBehaviour());
    }

    public ArrayList<Journey> getScheduledJourneys() {
        return scheduledJourneys;
    }

    private void addShutdownHook() {
        //Runtime.getRuntime().addShutdownHook(new Thread(this::printSchedule));
    }

    public void scheduleJourney(Proposal proposal) {
        this.scheduledJourneys.add(new Journey(proposal));
    }

    public int getEarliestAvailableTime() {
        if (scheduledJourneys.isEmpty()) {
            return 0;
        }
        return scheduledJourneys.get(scheduledJourneys.size() - 1).getEndTime();
    }

    public Point getLatestStartPoint() {
        if (scheduledJourneys.isEmpty())
            return startingPoint;
        else
            return scheduledJourneys.get(scheduledJourneys.size() - 1).getDropoffPoint();
    }

    public int getPickupDuration(Point pickupPoint) {
        return getLatestStartPoint().distanceTo(pickupPoint) / velocity;
    }

    public int getJourneyDuration(Point pickupPoint, Point dropoffPoint) {
        return pickupPoint.distanceTo(dropoffPoint) / velocity;
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
        synchronized (PlatformManager.getInstance().out()) {
            PrintWriter out = PlatformManager.getInstance().out();
            out.println("ROBOT: " + this.getLocalName());
            Table table = new Table(out, new String[]{"Time", "Journey", "Product"}, 50);
            for (Journey j : this.scheduledJourneys) {
                Object[] row = new Object[]{j.getStartTime() + "-" + j.getEndTime(), j.getStartPoint() + " -> " + j.getPickupPoint() + " -> " + j.getDropoffPoint(), j.getProductName()};

                table.addRow(row);
            }
            table.print();
            out.flush();
        }
    }

}
