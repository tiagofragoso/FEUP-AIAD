import java.io.Serializable;
import java.util.HashMap;

class Message implements Serializable {
	private HashMap<String, String> body;

	public Message(HashMap<String, String> body) {
		this.body = body;
	}

	public HashMap<String, String>  getBody() { return this.body; }
}