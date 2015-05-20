import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

  private Gson gson = new Gson();
  private DBInterface db = new DBInterface();

  // TODO change to POST
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Setup response
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    // Get user registration information
    String payload = request.getParameter("payload");
    RegisterRequest user = gson.fromJson(payload, RegisterRequest.class);

    // Validate registration
    if (!user.isValid()) {
      response.setStatus(400);
      out.print(gson.toJson(new ErrorResponse(
          "Invalid registration information.")));
      return;
    }

    // TODO: Write to database
    boolean success = db.insertUser(user.getEmail(), user.getPassword(), user.getFirstName(),
        user.getLastName());
    if (!success) {
      response.setStatus(400);
      out.print(gson.toJson(new ErrorResponse(
          "Database insertion failed.")));
      return;
    }

    // Return success status
    out.print(gson.toJson(new LoginResponse("test session id")));
  }
}
