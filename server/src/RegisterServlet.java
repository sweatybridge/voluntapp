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

  // TODO change to POST
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Setup response
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    // Get user registration information
    String data = request.getParameter("data");
    RegisterRequest user = gson.fromJson(data, RegisterRequest.class);

    // Validate registration
    if (!user.isValid()) {
      out.print(gson.toJson(new ErrorResponse(
          "Invalid registration information.")));
      return;
    }

    // TODO: Write to database

    // Return success status
    out.print(gson.toJson(new LoginResponse("test session id")));
  }
}
