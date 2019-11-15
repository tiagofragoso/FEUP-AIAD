package agents;

import behaviours.ReplyToRobotRequestBehaviour;
import behaviours.ScheduleJourneyBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.LoggableAgent;
import utils.Point;

import java.util.ArrayList;
import java.util.logging.Level;

public class RobotAgent extends LoggableAgent {

    private int velocity;
    private ArrayList<Journey> scheduledJourneys;
    private Point startingPoint;

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public ArrayList<Journey> getScheduledJourneys() {
        return scheduledJourneys;
    }

    public void setScheduledJourneys(ArrayList<Journey> scheduledJourneys) {
        this.scheduledJourneys = scheduledJourneys;
    }

    public Point getStartingPoint() {
        return startingPoint;
    }

    public void scheduleJourney(JourneyProposal proposal) {
        scheduledJourneys.add(new Journey(proposal));
    }

    public int getEarliestTimeAvailable() {
        if (scheduledJourneys.isEmpty()) {
            return 0;
        }
        return scheduledJourneys.get(scheduledJourneys.size()-1).getEndTime();
    }

    public Point getCurrentPoint() {
        if (scheduledJourneys.isEmpty()) {
            return startingPoint;
        }
        return scheduledJourneys.get(scheduledJourneys.size()-1).getDropoffPoint();
    }

    public void setStartingPoint(Point startingPoint) {
        this.startingPoint = startingPoint;
    }

    // TODO
    @Override
    public void printSchedule() {

    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::printSchedule));
    }

    protected void setup() {
        addShutdownHook();

        this.bootstrapAgent(this);

        log(Level.SEVERE, "Created");

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

        addBehaviour(new ReplyToRobotRequestBehaviour());
        addBehaviour(new ScheduleJourneyBehaviour());
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
