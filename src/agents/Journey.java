package agents;

import jade.core.AID;
import utils.Point;

public class Journey extends Job {

    private Point startPoint;
    private Point pickupPoint;
    private Point dropoffPoint;
    private int pickupTime;
    private int journeyDuration;


    public Journey(JourneyProposal proposal) {
        super(proposal.getProduct(), proposal.getRobot(), proposal.getRobotStartTime(), proposal.getProductStartTime() + proposal.getJourneyDuration());
        this.startPoint = proposal.getStartPoint();
        this.pickupPoint = proposal.getPickupMachine();
        this.dropoffPoint = proposal.getDropoffMachine();
        this.pickupTime = proposal.getProductStartTime();
        this.journeyDuration = proposal.getJourneyDuration();
    }

    public Point getDropoffPoint() {
        return dropoffPoint;
    }
}
