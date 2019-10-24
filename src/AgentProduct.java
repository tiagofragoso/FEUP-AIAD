import java.util.HashMap;

import jade.core.Agent;

public class AgentProduct extends Agent {

    private HashMap<Task, Integer>  tasks;
    private int priority;

    protected void setup() {
        System.out.println(this.getName());
        System.out.println("Hi--");

        tasks = new HashMap<>();
    }
}
