package agents;

import utils.Point;

public class Journey extends Job {

    private Point pickupPoint;
    private Point dropoffPoint;
    private Point startPoint;


    public Journey(Proposal proposal) {
        super(proposal.getJourneyProposal().getProduct(), proposal.getJourneyProposal().getRobot(), (proposal.getJourneyProposal().getProductStartTime() - proposal.getJourneyProposal().getPickupDuration()),
                (proposal.getJourneyProposal().getProductStartTime() + proposal.getJourneyProposal().getDuration()));
        this.pickupPoint = proposal.getJourneyProposal().getPickupPoint();
        this.dropoffPoint = proposal.getJourneyProposal().getDropoffPoint();
        this.startPoint = proposal.getJourneyProposal().getStartPoint();
    }

    public Point getPickupPoint() {
        return pickupPoint;
    }

    public Point getDropoffPoint() {
        return dropoffPoint;
    }

    public Point getStartPoint() {
        return startPoint;
    }
}
