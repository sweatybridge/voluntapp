package db;

public class Session {
	private String sessionId;
	
	public Session(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getId() {
		return sessionId;
	}
}
