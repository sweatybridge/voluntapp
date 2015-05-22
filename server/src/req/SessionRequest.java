package req;

public class SessionRequest implements Request {

  	private int userId;
  	private String sessionId;
  	
	
	public SessionRequest(int userId, String sessionId) {
		this.userId = userId;
		this.sessionId = sessionId;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public boolean isValid() {
		return true;
	}

}
