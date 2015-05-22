package db;

public class UserUpdate implements SQLInsert {
	
	public int userId;
	public String email;
	public String firstName;
	public String lastName;
	public String password;
	
	public UserUpdate(int userId, String email, String firstName, String lastName, String password) {
		this.userId = userId;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
	}

	@Override
	public String getSQLInsert() {
		String formatString = ((email==null)?"":"\"EMAIL\"='"+email+"',") +
			       ((firstName==null)?"":"\"FIRST_NAME\"='"+firstName+"',") +
			       ((lastName==null)?"":"\"LAST_NAME\"='"+lastName+"',") +
			       ((password==null)?"":"\"PASSWORD\"='"+password+"',");
		return String.format("UPDATE public.\"USERS\" SET %s WHERE \"ID\"=%d", 
			   formatString.substring(0, formatString.length()-1),userId);
	}
}
