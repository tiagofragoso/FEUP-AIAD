package agents;

import behaviours.CancelJourneyBehaviour;
import behaviours.JourneyProposalBehaviour;
import behaviours.ScheduleJourneyBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.LoggableAgent;
import utils.Point;
import utils.Table;

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
        addShutdownHook();

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
        Runtime.getRuntime().addShutdownHook(new Thread(this::printSchedule));
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

    public int getPickupDuration(Point pickupPoint) {
        if (scheduledJourneys.isEmpty()) {
            return startingPoint.distanceTo(pickupPoint) / velocity;
        } else {
            return scheduledJourneys.get(scheduledJourneys.size() - 1).getEndPoint().distanceTo(pickupPoint) / velocity;
        }
    }

    @Override
    public void printSchedule() {
        synchronized (System.out) {
            System.out.println("ROBOT: " + this.getLocalName());
            Table table = new Table(new String[]{"Time", "Journey", "Product"});
            for (Journey j : this.scheduledJourneys) {
                Object[] row = new Object[]{j.getStartTime() + "-" + j.getEndTime(), j.getStartPoint() + " -> " + j.getEndPoint(), j.getProductName()};

                table.addRow(row);
            }
            table.print();
        }
    }

}
