package tp1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import util.ConvertError;

import java.net.URI;
import java.util.List;

import static tp1.api.service.rest.RestUsers.QUERY;

@SuppressWarnings("unchecked")
public class RestUsersClient extends RestClient implements Users {

	final WebTarget target;

	public RestUsersClient(URI serverURI) {
		super(serverURI);
		target = client.target(serverURI).path(RestUsers.PATH);
	}

	@Override
	public Result<String> createUser(User user) {
		return super.reTry(() -> clt_createUser(user));
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		return super.reTry(() -> clt_getUser(userId, password));
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		return super.reTry(() -> clt_updateUser(userId, password, user));
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		return super.reTry(() -> clt_deleteUser(userId, password));
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return super.reTry(() -> clt_searchUsers(pattern));
	}

	private Result<User> clt_getUser(String userId, String password) {
		Response response = target.path(userId)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok(response.readEntity(User.class));
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}


	private Result<User> clt_updateUser(String userId, String password, User user) {
		Response response = target.path(userId)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON));

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok(response.readEntity(User.class));
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}

	private Result<User> clt_deleteUser(String userId, String password) {
		Response response = target.path(userId)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok(response.readEntity(User.class));
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}

	private Result<String> clt_createUser(User user) {
		Response response = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok(response.readEntity(String.class));
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}

	private Result<List<User>> clt_searchUsers(String pattern) {
		Response response = target
				.queryParam(QUERY, pattern)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok(response.readEntity(new GenericType<>() {
			}));
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}
}
