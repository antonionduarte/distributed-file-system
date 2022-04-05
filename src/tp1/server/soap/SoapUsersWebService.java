package tp1.server.soap;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.jws.WebService;
import tp1.api.User;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;

@WebService(serviceName = SoapUsers.NAME, targetNamespace = SoapUsers.NAMESPACE, endpointInterface = SoapUsers.INTERFACE)
public class SoapUsersWebService implements SoapUsers {

	private static final String CONFLICT = "Conflict";
	private static final String BAD_REQUEST = "Bad Request";

	static Logger Log = Logger.getLogger(SoapUsersWebService.class.getName());

	final protected Map<String, User> users = new HashMap<>();

	public SoapUsersWebService() {
	}

	@Override
	public String createUser(User user) throws UsersException {
		Log.info(String.format("SOAP createUser: user = %s\n", user));

		if (badUserData(user))
			throw new UsersException(BAD_REQUEST);

		var userId = user.getUserId();
		var res = users.putIfAbsent(userId, user);

		if (res != null)
			throw new UsersException(CONFLICT);
		else {
//			Sleep.ms( 5000 );
			return userId;
		}
	}

	@Override
	public User getUser(String userId, String password) throws UsersException {
		throw new RuntimeException("Not Implemented...");
	}

	@Override
	public User updateUser(String userId, String password, User user) throws UsersException {
		throw new RuntimeException("Not Implemented...");
	}

	@Override
	public User deleteUser(String userId, String password) throws UsersException {
		throw new RuntimeException("Not Implemented...");
	}

	@Override
	public List<User> searchUsers(String pattern) throws UsersException {
		throw new RuntimeException("Not Implemented...");
	}


	private boolean badUserData(User user) {
		//TODO check user data...
		return false;
	}

}
