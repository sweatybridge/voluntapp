package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.RegisterRequest;
import req.UserRequest;
import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;
import resp.SuccessResponse;
import resp.UserResponse;
import utils.EmailUtils;
import utils.PasswordUtils;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import db.CodeGenerator;
import db.DBInterface;
import exception.InconsistentDataException;
import exception.PasswordHashFailureException;
import exception.UserNotFoundException;

/**
 * Handles API requests to user resources.
 */
@WebServlet
public class UserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final Gson gson;
	private final DBInterface db;

	/**
	 * Constructs a user servlet with injected dependencies.
	 * 
	 * @param gson
	 *            json serialiser
	 * @param db
	 *            database interface
	 */
	public UserServlet(Gson gson, DBInterface db) {
		this.gson = gson;
		this.db = db;
	}

	/**
	 * Retrieve details of the current user.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		// Session should not be null if user is authenticated
		SessionResponse session = (SessionResponse) request
				.getAttribute(SessionResponse.class.getSimpleName());

		Response resp;
		try {
			int userId = session.getUserId();
			resp = db.getUser(new UserRequest(userId));
		} catch (UserNotFoundException e) {
			resp = new ErrorResponse("User is deleted but session is active.");
		} catch (SQLException | InconsistentDataException e) {
			resp = new ErrorResponse(e.getMessage());
		}

		request.setAttribute(Response.class.getSimpleName(), resp);
	}

	/**
	 * TODO: Delete current user from the database.
	 */
	@Override
	public void doDelete(HttpServletRequest request,
			HttpServletResponse response) {

		// get current user id from auth token
		SessionResponse session = (SessionResponse) request
				.getAttribute(SessionResponse.class.getSimpleName());

		Response resp = new SuccessResponse(
				"Successfully deleted user from database.");

		request.setAttribute(Response.class.getSimpleName(), resp);
	}

	/**
	 * Updates the user details with supplied information.
	 */
	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) {
		// First get the user id from the session
		SessionResponse session = (SessionResponse) request
				.getAttribute(SessionResponse.class.getSimpleName());
		int uid = session.getUserId();

		// Create a register request and try to update the database with the
		// given user
		Response resp = new ErrorResponse("Unknown error occurred.");
		RegisterRequest rr = null;
		try {
			// Try parsing the request
			rr = gson.fromJson(request.getReader(),
					RegisterRequest.class);
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			e.printStackTrace();
			resp = new ErrorResponse("Invalid update payload.");
			return;
		}
			// Make sure it is a valid request
			if (rr.isValid()) {
				try {
					// Check the current password
					UserResponse uresp = db.getUser(new UserRequest(uid));
					if (!PasswordUtils.validatePassword(
							rr.getCurrentPassword(), uresp.getHashedPassword())) {
						resp = new ErrorResponse(
								"Your current password doesn't match our records, have you entered it correctly?");
					} else {
						// Everything seems to be fine, try to update the
						// database
						rr.hashPassword(); // Hash the password first
						db.updateUser(uid, rr);
						resp = new SuccessResponse("Successfully updated user.");
					}
				} catch (SQLException | InconsistentDataException
						| UserNotFoundException | PasswordHashFailureException e) {
					e.printStackTrace();
					resp = new ErrorResponse("Error in user update sequence.");
				}
			}

		request.setAttribute(Response.class.getSimpleName(), resp);
	}

	/**
	 * Registers the user with database.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		// Parse user registration request
		RegisterRequest user = gson.fromJson(request.getReader(),
				RegisterRequest.class);

		// Validate registration
		if (!user.isValid()) {
			request.setAttribute(
					Response.class.getSimpleName(),
					new ErrorResponse(
							"You have entered invalid registration information."));
			return;
		}

		// Create a new validation code
		CodeGenerator cg = new CodeGenerator();
		String validationCode = cg
				.getCode(ValidationServlet.VALIDATION_CODE_LENGTH);

		try {

			// Write to database
			int userId = db.putUser(user, validationCode);

			EmailUtils.sendValidationEmail(user.getEmail(), validationCode);

			// Forward to session servlet
			// request.setAttribute("userId", userId);
			// getServletContext().getRequestDispatcher("/api/session").forward(request,
			// response);

			request.setAttribute(Response.class.getSimpleName(),
					new SuccessResponse());
		} catch (SQLException e) {
			e.printStackTrace();
			request.setAttribute(Response.class.getSimpleName(),
					new ErrorResponse(
							"The email you entered is already in use."));
		} catch (PasswordHashFailureException e) {
			request.setAttribute(Response.class.getSimpleName(),
					new ErrorResponse("Password Hashing Failed"));
		}
	}
}
