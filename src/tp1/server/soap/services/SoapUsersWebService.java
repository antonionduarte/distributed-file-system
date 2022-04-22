package tp1.server.soap.services;

import java.util.List;

import jakarta.jws.WebService;
import tp1.api.User;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;
import tp1.api.service.util.Users;
import tp1.server.JavaUsers;

@WebService(serviceName = SoapUsers.NAME, targetNamespace = SoapUsers.NAMESPACE, endpointInterface = SoapUsers.INTERFACE)
public class SoapUsersWebService implements SoapUsers {

	final Users impl = new JavaUsers();

	public SoapUsersWebService() {
	}

	@Override
	public String createUser(User user) throws UsersException {
		var result = impl.createUser(user);
		if (result.isOK()) {
			return result.value();
		} else {
			throw new UsersException(result.error().toString());
		}
	}

	@Override
	public User getUser(String userId, String password) throws UsersException {
		var result = impl.getUser(userId, password);
		if (result.isOK()) {
			return result.value();
		} else {
			throw new UsersException(result.error().toString());
		}
	}

	@Override
	public User updateUser(String userId, String password, User user) throws UsersException {
		var result = impl.updateUser(userId, password, user);
		if (result.isOK()) {
			return result.value();
		} else {
			throw new UsersException(result.error().toString());
		}
	}

	@Override
	public User deleteUser(String userId, String password) throws UsersException {
		var result = impl.deleteUser(userId, password);
		if (result.isOK()) {
			return result.value();
		} else {
			throw new UsersException(result.error().toString());
		}
	}

	@Override
	public List<User> searchUsers(String pattern) throws UsersException {
		var result = impl.searchUsers(pattern);
		if (result.isOK()) {
			return result.value();
		} else {
			throw new UsersException(result.error().toString());
		}
	}
}
