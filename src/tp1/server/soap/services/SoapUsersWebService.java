package tp1.server.soap.services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.jws.WebService;
import tp1.api.User;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;
import tp1.api.service.util.Result;

@WebService(serviceName = SoapUsers.NAME, targetNamespace = SoapUsers.NAMESPACE, endpointInterface = SoapUsers.INTERFACE)
public class SoapUsersWebService implements SoapUsers {

	static Logger Log = Logger.getLogger(SoapUsersWebService.class.getName());

	final protected Map<String, User> users = new HashMap<>();

	public SoapUsersWebService() {
	}

	@Override
	public String createUser(User user) throws UsersException {
		Log.info(String.format("SOAP createUser: user = %s\n", user));

		if (badUserData(user))
			throw new UsersException(Result.ErrorCode.BAD_REQUEST.toString());

		var userId = user.getUserId();
		var res = users.putIfAbsent(userId, user);

		if (res != null)
			throw new UsersException(Result.ErrorCode.CONFLICT.toString());

		return userId;
	}

	@Override
	public User getUser(String userId, String password) throws UsersException {
		Log.info(String.format("SOAP getUser: userId = %s, password = %s\n", userId, password));

		// Check if user is valid
		if (userId == null) {
			Log.info("UserId or password null.");
			throw new UsersException(Result.ErrorCode.BAD_REQUEST.toString());
		}

		User user = users.get(userId);

		// Check if user exists
		if (user == null) {
			Log.info("User does not exist.");
			throw new UsersException(Result.ErrorCode.NOT_FOUND.toString());
		}

		// Check if the password is correct
		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new UsersException(Result.ErrorCode.FORBIDDEN.toString());
		}

		return user;
	}

	@Override
	public User updateUser(String userId, String password, User user) throws UsersException {
		Log.info("SOAP updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);

		if (userId == null) {
			Log.info("UserId null.");
			throw new UsersException(Result.ErrorCode.BAD_REQUEST.toString());
		}

		User existingUser = users.get(userId);

		if (existingUser == null) {
			Log.info("User does not exist.");
			throw new UsersException(Result.ErrorCode.NOT_FOUND.toString());
		}

		if (!existingUser.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new UsersException(Result.ErrorCode.FORBIDDEN.toString());
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
	public User deleteUser(String userId, String password) throws UsersException {
		Log.info("SOAP deleteUser : user = " + userId + "; pwd = " + password);

		if (userId == null) {
			Log.info("UserId null.");
			throw new UsersException(Result.ErrorCode.BAD_REQUEST.toString());
		}

		User user = users.get(userId);

		if (user == null) {
			Log.info("User does not exist.");
			throw new UsersException(Result.ErrorCode.NOT_FOUND.toString());
		}

		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new UsersException(Result.ErrorCode.FORBIDDEN.toString());
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


	private boolean badUserData(User user) {
		return (user.getUserId() == null || user.getPassword() == null || user.getFullName() == null ||
				user.getEmail() == null);
	}

}
