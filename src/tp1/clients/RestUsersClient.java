package tp1.clients;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;

public class RestUsersClient extends RestClient implements RestUsers {

	final WebTarget target;

	public RestUsersClient(URI serverURI) {
		super(serverURI);
		target = client.target(serverURI).path(RestUsers.PATH);
	}

	@Override
	public String createUser(User user) {
		return super.reTry(() -> {
			return clt_createUser(user);
		});
	}

	@Override
	public User getUser(String userId, String password) {
		return super.reTry(() -> {
			return clt_getUser(userId, password);
		});
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		return super.reTry(() -> {
			return clt_updateUser(userId, password, user);
		});
	}

	@Override
	public User deleteUser(String userId, String password) {
		return super.reTry(() -> {
			return clt_deleteUser(userId, password);
		});
	}

	@Override
	public List<User> searchUsers(String pattern) {
		return super.reTry(() -> clt_searchUsers(pattern));
	}

	private User clt_getUser(String userId, String password) {
		Response response = target.path(userId)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity()) {
			System.out.println("Success:");
			User user = response.readEntity(User.class);
			System.out.println("User : " + user);
			return user;
		} else
			System.out.println("Error, HTTP error status: " + response.getStatus());

		return null;
	}


	private User clt_updateUser(String userId, String password, User user) {
		Response response = target.path(userId)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON));

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity())
			return response.readEntity(User.class);
		else
			System.out.println("Error, HTTP error status: " + response.getStatus());

		return null;
	}

	private User clt_deleteUser(String userId, String password) {
		Response response = target.path(userId)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity())
			return response.readEntity(User.class);
		else
			System.out.println("Error, HTTP error status: " + response.getStatus());

		return null;
	}

	private String clt_createUser(User user) {
		Response response = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity())
			return response.readEntity(String.class);
		else
			System.out.println("Error, HTTP error status: " + response.getStatus());

		return null;
	}

	private List<User> clt_searchUsers(String pattern) {
		Response response = target
				.queryParam(QUERY, pattern)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity())
			return response.readEntity(new GenericType<List<User>>() {
			});
		else
			System.out.println("Error, HTTP error status: " + response.getStatus());

		return null;
	}
}
