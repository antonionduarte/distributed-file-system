package tp1.server.rest.resources;

import java.util.*;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Users;
import tp1.server.JavaUsers;

@Singleton
public class UsersResource implements RestUsers {

	final Users impl = new JavaUsers();

	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());

	public UsersResource() {
	}

	@Override
	public String createUser(User user) {
		var result = impl.createUser(user);

		if (result.isOK()) {
			return result.value();
		} else {
			var errorCode = ConvertError.convertError(result);
			throw new WebApplicationException(errorCode);
		}
	}


	@Override
	public User getUser(String userId, String password) {
		var result = impl.getUser(userId, password);

		if (result.isOK()) {
			return result.value();
		} else {
			var errorCode = ConvertError.convertError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		var result = impl.updateUser(userId, password, user);

		if (result.isOK()) {
			return result.value();
		} else {
			var errorCode = ConvertError.convertError(result);
			throw new WebApplicationException(errorCode);
		}
	}


	@Override
	public User deleteUser(String userId, String password) {
		var result = impl.deleteUser(userId, password);

		if (result.isOK()) {
			return result.value();
		} else {
			var errorCode = ConvertError.convertError(result);
			throw new WebApplicationException(errorCode);
		}
	}


	@Override
	public List<User> searchUsers(String pattern) {
		var result = impl.searchUsers(pattern);

		if (result.isOK()) {
			return result.value();
		} else {
			var errorCode = ConvertError.convertError(result);
			throw new WebApplicationException(errorCode);
		}
	}

}
