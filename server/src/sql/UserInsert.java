package sql;

public class UserInsert implements SQLUpdate {
  
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
  public String getSQLUpdate() {
    return "INSERT INTO public.\"USERS\" VALUES(DEFAULT, '" + email + "','" 
        + password + "','" + firstName + "','" + lastName + "', DEFAULT);";
  }

  @Override
  public void checkResult(int rowsAffected) {
    // TODO Auto-generated method stub
    
  }

}
