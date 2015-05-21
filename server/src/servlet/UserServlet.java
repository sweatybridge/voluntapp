package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.LoginRequest;
import req.RegisterRequest;
import resp.ErrorResponse;
import resp.LoginResponse;
import resp.SuccessResponse;

import com.google.gson.Gson;

import db.DBInterface;

@WebServlet("/user")
public class UserServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  // private static final Logger logger = Logger.getLogger("UserServlet");

  // TODO: share these objects with singleton pattern
  private Gson gson = new Gson();
  private DBInterface db = new DBInterface();

  // Retrieves current user details
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Setup response
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    // TODO get current user id from auth token
    // TODO retrieve user info from database

    out.print("user info.");
  }

  // Delete the user from database
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Setup response
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    // TODO get current user id from auth token
    // TODO delete from user table

    out.print(gson.toJson(new SuccessResponse(
        "Successfully deleted user from database.")));
  }

  // Register the user
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Setup response
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    // Get user registration information
    String body = request.getReader().readLine().substring("payload=".length());
    String payload = URLDecoder.decode(body, "UTF-8");

    RegisterRequest user = gson.fromJson(payload, RegisterRequest.class);

    // Validate registration
    if (!user.isValid()) {
      response.setStatus(400);
      out.print(gson.toJson(new ErrorResponse(
          "You have entered invalid registration information.")));
      return;
    }

    // Write to database
    boolean success =
        db.insertUser(user.getEmail(), user.getPassword(), user.getFirstName(),
            user.getLastName());
    if (!success) {
      response.setStatus(400);
      out.print(gson.toJson(new ErrorResponse(
          "The email you entered is already in use.")));
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
    String data = request.getParameter("payload");
    LoginRequest user = gson.fromJson(data, LoginRequest.class);

    // Validate registration
    if (!user.isValid()) {
      out.print(gson.toJson(new ErrorResponse(
          "You have entered invalid login information.")));
      return;
    }

    // TODO: Read from database

    // Return success status
    out.print(gson.toJson(new LoginResponse("test session id")));
  }
}
