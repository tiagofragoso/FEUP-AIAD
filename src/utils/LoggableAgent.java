package utils;

import jade.core.Agent;
import jade.util.Logger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class LoggableAgent extends Agent {
    public static boolean severeOnly = true;
    private Logger logger;

    protected void bootstrapAgent(Agent agent) {
        this.logger = Logger.getMyLogger(agent.getLocalName());
        logger.setUseParentHandlers(false);
        CustomFormatter formatter = new CustomFormatter(agent);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
    }

    public void log(Level level, String msg) {
        if (severeOnly && level != Level.SEVERE) {
            return;
        }
        logger.log(level, msg);
    }
}
