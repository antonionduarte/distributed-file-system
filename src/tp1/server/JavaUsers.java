package tp1.server;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.User;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.ClientFactory;
import tp1.server.rest.resources.UsersResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JavaUsers implements Users {

	private final Map<String, User> users;

	private final ClientFactory clientFactory;

	private static final Logger Log = Logger.getLogger(JavaUsers.class.getName());

	public JavaUsers() {
		this.users = new HashMap<>();
		this.clientFactory = ClientFactory.getInstance();
	}

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);

		// Check if user data is valid
		if (user.getUserId() == null || user.getPassword() == null || user.getFullName() == null ||
				user.getEmail() == null) {
			Log.info("User object invalid.");

			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}

		synchronized (this) {
			// Check if userId already exists
			if (users.containsKey(user.getUserId())) {
				Log.info("User already exists.");

				return Result.error(Result.ErrorCode.CONFLICT);
			}

			// Add the user to the map of users
			users.put(user.getUserId(), user);
		}

		return Result.ok(user.getUserId());
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);

		Result<User> valid = validateUser(userId, password);
		if(!valid.isOK())
			return valid;

		return Result.ok(valid.value());
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);

		Result<User> valid = validateUser(userId, password);
		if(!valid.isOK())
			return valid;

		User existingUser = valid.value();

		user.setUserId(existingUser.getUserId());

		if (user.getEmail() == null) {
			user.setEmail(existingUser.getEmail());
		}
		if (user.getFullName() == null) {
			user.setFullName(existingUser.getFullName());
		}
		if (user.getEmail() == null) {
			user.setEmail(existingUser.getEmail());
		}
		if (user.getPassword() == null) {
			user.setPassword(existingUser.getPassword());
		}

		users.put(userId, user);

		return Result.ok(user);
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);

		Result<User> valid;
		synchronized (this) {
			valid = validateUser(userId, password);
			if (!valid.isOK())
				return valid;

			users.remove(userId);
		}

		Directory directoryClient = clientFactory.getDirectoryClient().second();
		directoryClient.removeUser(userId);

		return Result.ok(valid.value());
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);

		List<User> users = new ArrayList<>();

		if (pattern == null || pattern.length() == 0) {
			users.addAll(this.users.values());
			return Result.ok(users);
		}

		if (this.users.isEmpty()) {
			return Result.ok(users);
		}

		for (User nextUser : this.users.values()) {
			if (nextUser.getFullName().toLowerCase().contains(pattern.toLowerCase())) {
				User cleansedUser = new User(nextUser.getUserId(), nextUser.getFullName(),
						nextUser.getEmail(), "");
				users.add(cleansedUser);
			}
		}

		return Result.ok(users);
	}

	private Result<User> validateUser(String userId, String password) {
		if (userId == null) {
			Log.info("UserId or password null.");
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}

		User user = users.get(userId);

		if (user == null) {
			Log.info("User does not exist.");
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}

		return Result.ok(user);
	}
}
