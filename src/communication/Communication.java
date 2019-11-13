package communication;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.HashMap;

public class Communication {

    public static void prepareMessage(HashMap<String, String> body, ACLMessage msg, String conversationId, String value) {
        setBody(body, msg);
        msg.setConversationId(conversationId);
        msg.setReplyWith(value); // Unique value
    }

    public static MessageTemplate prepareMessageTemplate(ACLMessage msg, String conversationId) {
        return MessageTemplate.and(MessageTemplate.MatchConversationId(conversationId),
                MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
    }

    public static void setBody(HashMap<String, String> body, ACLMessage msg) {
        try {
            msg.setContentObject(
                    new Message(body)
            );
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
    }

}
