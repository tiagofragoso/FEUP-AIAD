package communication;

import java.io.Serializable;
import java.util.HashMap;

public class Message implements Serializable {
    private HashMap<String, Object> body = new HashMap<>();

    public Message() {
    }

    public Message(HashMap<String, Object> body) {
        this.body = body;
    }

    public HashMap<String, Object> getBody() {
        return this.body;
    }

    public void append(String key, Object object) {
        this.body.put(key, object);
    }
}