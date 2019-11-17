package agents;

import jade.core.AID;

import java.io.Serializable;

class Job implements Serializable {
    private int startTime;
    private int endTime;
    private AID product;
    private AID worker;

    Job(AID product, AID worker, int startTime, int endTime) {
        this.product = product;
        this.worker = worker;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getStartTime() {
        return startTime;
    }

    int getEndTime() {
        return endTime;
    }

    AID getProduct() {
        return product;
    }

    String getProductName() {
        return product.getLocalName();
    }

    AID getWorker() {
        return worker;
    }

    String getWorkerName() {
        return worker.getLocalName();
    }
}
