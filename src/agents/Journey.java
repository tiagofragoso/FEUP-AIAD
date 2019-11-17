package agents;

import jade.core.AID;
import utils.Point;

public class Journey extends Job {

    private Point startPoint;
    private Point endPoint;



    public Journey(Proposal proposal) {
        super(proposal.getJourneyProposal().getProduct(), proposal.getJourneyProposal().getRobot(), (proposal.getJourneyProposal().getProductStartTime() - proposal.getJourneyProposal().getPickupDuration()),
                (proposal.getJourneyProposal().getProductStartTime()+ proposal.getJourneyProposal().getDuration()));
        this.startPoint = proposal.getJourneyProposal().getPickupPoint();
        this.endPoint = proposal.getJourneyProposal().getDropoffPoint();
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }
}
