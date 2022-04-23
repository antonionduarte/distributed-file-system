package tp1.clients.soap;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import tp1.api.User;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

public class SoapUsersClient extends SoapClient implements Users {
	private final SoapUsers users;

	public SoapUsersClient(URI serverURI) throws MalformedURLException {
		super(serverURI);
		QName qname = new QName(SoapUsers.NAMESPACE, SoapUsers.NAME);
		Service service = Service.create(URI.create(serverURI + "?wsdl").toURL(), qname);
		users = service.getPort(tp1.api.service.soap.SoapUsers.class);
		SoapClient.setTimeouts((BindingProvider) users);
	}

	@Override
	public Result<String> createUser(User user) {
		return super.reTry(() -> {
			try {
				return Result.ok(users.createUser(user));
			} catch (UsersException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		return super.reTry(() -> {
			try {
				return Result.ok(users.getUser(userId, password));
			} catch (UsersException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		return super.reTry(() -> {
			try {
				return Result.ok(users.updateUser(userId, password, user));
			} catch (UsersException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		return super.reTry(() -> {
			try {
				return Result.ok(users.deleteUser(userId, password));
			} catch (UsersException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return super.reTry(() -> {
			try {
				return Result.ok(users.searchUsers(pattern));
			} catch (UsersException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}
}
