package tp1.server.resources;

import java.util.*;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;

@Singleton
public class UsersResource implements RestUsers {

	private final Map<String, User> users = new HashMap<>();

	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());

	public UsersResource() {}

	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);

		// Check if user data is valid
		if (user.getUserId() == null || user.getPassword() == null || user.getFullName() == null ||
				user.getEmail() == null) {
			Log.info("User object invalid.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// Check if userId already exists
		if (users.containsKey(user.getUserId())) {
			Log.info("User already exists.");
			throw new WebApplicationException(Status.CONFLICT);
		}

		// Add the user to the map of users
		users.put(user.getUserId(), user);
		return user.getUserId();
	}


	@Override
	public User getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);

		// Check if user is valid
		if (userId == null) {
			Log.info("UserId or passwrod null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User user = users.get(userId);

		// Check if user exists 
		if (user == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		// Check if the password is correct
		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		return user;
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);

		if (userId == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User existingUser = users.get(userId);

		if (existingUser == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		if (!existingUser.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		user.setUserId(existingUser.getUserId());

		if (user.getEmail() == null) user.setEmail(existingUser.getEmail());
		if (user.getFullName() == null) user.setFullName(existingUser.getFullName());
		if (user.getEmail() == null) user.setEmail(existingUser.getEmail());
		if (user.getPassword() == null) user.setPassword(existingUser.getPassword());

		users.put(userId, user);

		return user;
	}


	@Override
	public User deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);

		if (userId == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User user = users.get(userId);

		if (user == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		users.remove(userId);

		return user;
	}


	@Override
	public List<User> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);

		List<User> users = new ArrayList<>();

		if (pattern == null || pattern.length() == 0) {
			users.addAll(this.users.values());
			return users;
		}

		if (this.users.isEmpty()) return users;

		for (User nextUser : this.users.values()) {
			if (nextUser.getFullName().toLowerCase().contains(pattern.toLowerCase())) {
				User cleansedUser = new User(nextUser.getUserId(), nextUser.getFullName(),
						nextUser.getEmail(), "");
				users.add(cleansedUser);
			}
		}

		return users;
	}

}
