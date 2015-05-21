package db;

public class UserInsert implements SQLInsert {
  
  private String email; 
  private String password; 
  private String firstName; 
  private String lastName;
  
  public UserInsert(String email, String password, String firstName, String lastName) {
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
  }
  
  @Override
  public String getSQLInsert() {
    return "INSERT INTO public.\"USERS\" VALUES(DEFAULT, '" + email + "','" 
        + password + "','" + firstName + "','" + lastName + "', DEFAULT);";
  }

}
