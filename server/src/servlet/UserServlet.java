package servlet;

import java.io.IOException;
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
import resp.Response;
import resp.SuccessResponse;

import com.google.gson.Gson;

import db.DBInterface;

@WebServlet
public class UserServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  // private static final Logger logger = Logger.getLogger("UserServlet");

  private final Gson gson;
  private final DBInterface db;

  public UserServlet(Gson gson, DBInterface db) {
    this.gson = gson;
    this.db = db;
  }

  // Retrieves current user details
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Setup response

    // TODO get current user id from auth token
    // TODO retrieve user info from database

    response.getWriter().print("user info.");
  }

  // Delete the user from database
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Setup response
    Response apiResponse = null;

    // TODO get current user id from auth token
    // TODO delete from user table

    if (apiResponse == null) {
      apiResponse =
          new SuccessResponse("Successfully deleted user from database.");
    }

    response.getWriter().print(gson.toJson(apiResponse));
  }

  // Register the user
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Setup response
    Response apiResponse = null;

    // Get user registration information
    String body = request.getReader().readLine().substring("payload=".length());
    String payload = URLDecoder.decode(body, "UTF-8");

    RegisterRequest user = gson.fromJson(payload, RegisterRequest.class);

    // Validate registration
    if (!user.isValid()) {
      response.setStatus(400);
      apiResponse =
          new ErrorResponse(
              "You have entered invalid registration information.");
    }

    // Write to database
    boolean success =
        db.insertUser(user.getEmail(), user.getPassword(), user.getFirstName(),
            user.getLastName());
    if (!success) {
      response.setStatus(400);
      apiResponse =
          new ErrorResponse("The email you entered is already in use.");
    }

    // TODO: create new session

    // Return success status
    if (apiResponse == null) {
      apiResponse = new LoginResponse("test session id");
    }

    response.getWriter().print(gson.toJson(apiResponse));
  }

  // Logs in the user
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Setup response
    Response apiResponse = null;

    // Get user registration information
    String data = request.getParameter("payload");
    LoginRequest user = gson.fromJson(data, LoginRequest.class);

    // Validate registration
    if (!user.isValid()) {
      apiResponse =
          new ErrorResponse("You have entered invalid login information.");
    }

    // TODO: Read from database

    if (apiResponse == null) {
      apiResponse = new LoginResponse("test session id");
    }

    // Return success status
    response.getWriter().print(gson.toJson(apiResponse));
  }
}
