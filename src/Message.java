import java.io.Serializable;
import java.util.HashMap;

class Message implements Serializable {
	public enum message_type { REQUEST, AVAILABE, NOT_AVAILABLE, CONFIRMATION, SCHEDULED };

	private message_type type;
	private HashMap<String, String> body;

	public Message(message_type type, HashMap<String, String> body) {
		this.type = type;
		this.body = body;
	}

	public message_type getType() { return this.type; }

	public HashMap<String, String>  getBody() { return this.body; }
}