package agents;

import jade.core.AID;

class Job {
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

    int getStartTime() {
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
