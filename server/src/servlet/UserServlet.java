package servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
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
import db.UserInsert;

/**
 * Handles API requests to user resources.
 */
@WebServlet
public class UserServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  // private static final Logger logger = Logger.getLogger("UserServlet");

  private final Gson gson;
  private final DBInterface db;

  /**
   * Constructs a user servlet with injected dependencies.
   * 
   * @param gson json serialiser
   * @param db database interface
   */
  public UserServlet(Gson gson, DBInterface db) {
    this.gson = gson;
    this.db = db;
  }

  /**
   * Retrieve details of the current user.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // TODO get current user id from auth token
    // TODO retrieve user info from database

    response.getWriter().print("user info."); //$NON-NLS-1$
  }

  /**
   * Delete current user from the database.
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // TODO get current user id from auth token
    // TODO delete from user table

    Response resp =
        new SuccessResponse(Messages.getString("UserServlet.deleteSuccess")); //$NON-NLS-1$

    response.getWriter().print(gson.toJson(resp));
  }

  /**
   * Register a new user with supplied information.
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    // Parse user register request
    String body = request.getReader().readLine().substring("payload=".length()); //$NON-NLS-1$
    String payload = URLDecoder.decode(body, "UTF-8"); //$NON-NLS-1$
    RegisterRequest req = gson.fromJson(payload, RegisterRequest.class);

    // Handle request in a RESTful manner
    Response resp = handle(req);
    if (resp instanceof ErrorResponse) {
      response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    // Write response to output
    response.getWriter().print(gson.toJson(resp));
  }

  /**
   * Logs in the user with email and password.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    // Parse user login request
    String data = request.getParameter("payload"); //$NON-NLS-1$
    LoginRequest req = gson.fromJson(data, LoginRequest.class);

    // Handle request in a RESTful manner
    Response resp = handle(req);
    if (resp instanceof ErrorResponse) {
      response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    // Write response to output
    response.getWriter().print(gson.toJson(resp));
  }

  private Response handle(RegisterRequest req) {
    // Validate registration
    if (!req.isValid()) {
      return new ErrorResponse(
          Messages.getString("UserServlet.registerInvalid")); //$NON-NLS-1$
    }

    // Write to database
    boolean success =
        db.insert(new UserInsert(req.getEmail(), req.getPassword(), req
            .getFirstName(), req.getLastName()));
    if (!success) {
      return new ErrorResponse(Messages.getString("UserServlet.registerInUse")); //$NON-NLS-1$
    }

    // TODO: create new session

    // Successfully registered
    return new LoginResponse("test session id"); //$NON-NLS-1$
  }

  private Response handle(LoginRequest req) {
    // Validate login
    if (!req.isValid()) {
      return new ErrorResponse(Messages.getString("UserServlet.loginInvalid")); //$NON-NLS-1$
    }

    // TODO: Read from database

    // Successfully logged in
    return new LoginResponse("test session id"); //$NON-NLS-1$
  }
}
