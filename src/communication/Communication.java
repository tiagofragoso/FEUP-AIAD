package communication;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;

public class Communication {

    public static void prepareMessage(Message contentObject, ACLMessage msg, String conversationId, String value) {
        setContentObject(contentObject, msg);
        msg.setConversationId(conversationId);
        msg.setReplyWith(value); // Unique value
    }

    public static MessageTemplate prepareMessageTemplate(ACLMessage msg, String conversationId) {
        return MessageTemplate.and(MessageTemplate.MatchConversationId(conversationId),
                MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
    }

    public static void setContentObject(Message contentObject, ACLMessage msg) {
        try {
            msg.setContentObject(contentObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
