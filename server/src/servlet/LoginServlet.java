package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.LoginRequest;
import resp.ErrorResponse;
import resp.LoginResponse;

import com.google.gson.Gson;


@WebServlet("/login")
public class LoginServlet extends HttpServlet {

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
    LoginRequest user = gson.fromJson(data, LoginRequest.class);

    // Validate registration
    if (!user.isValid()) {
      out.print(gson.toJson(new ErrorResponse("Invalid login information.")));
      return;
    }

    // TODO: Read from database

    // Return success status
    out.print(gson.toJson(new LoginResponse("test session id")));
  }
}
