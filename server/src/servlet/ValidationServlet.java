package servlet;

import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import resp.ErrorResponse;
import resp.Response;
import resp.SuccessResponse;

import com.google.gson.Gson;

import db.DBInterface;

public class ValidationServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private final DBInterface db;
  private final Gson gson;

  public ValidationServlet(Gson gson, DBInterface db) {
    this.db = db;
    this.gson = gson;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    String email = (String) request.getAttribute("email");
    String validationCode = (String) request.getAttribute("validationCode");

    Boolean valid;
    try {
      valid = db.checkValidation(email, validationCode);
    } catch (SQLException e) {
      e.printStackTrace();
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Validation check failed"));
      return;
    }
    // Check whether the validation code was correct and return relevant
    // information to the client.
    if (!valid) {
      response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
      request
          .setAttribute(
              Response.class.getSimpleName(),
              new ErrorResponse(
                  "You have entered an invalid validation key, please check your email"));
      return;
    } else {
      request.setAttribute(Response.class.getSimpleName(), new SuccessResponse(
          "Correct validation code!"));
      return;
    }
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {

  }

}
