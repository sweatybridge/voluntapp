package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.LoginRequest;
import req.RegisterRequest;
import resp.ErrorResponse;
import resp.LoginResponse;

import com.google.gson.Gson;

import db.DBInterface;

@WebServlet("/user")
public class UserServlet extends HttpServlet {

  private Gson gson = new Gson();
  private DBInterface db = new DBInterface();

  // TODO: Retrieves current user details
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }

  // TODO: Delete the user from database
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }

  // Register the user
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
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

    // Write to database
    boolean success =
        db.insertUser(user.getEmail(), user.getPassword(), user.getFirstName(),
            user.getLastName());
    if (!success) {
      response.setStatus(400);
      out.print(gson.toJson(new ErrorResponse("Database insertion failed.")));
      return;
    }

    // TODO: create new session

    // Return success status
    out.print(gson.toJson(new LoginResponse("test session id")));
  }

  // Logs in the user
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
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
