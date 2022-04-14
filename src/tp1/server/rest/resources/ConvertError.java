package tp1.server.rest.resources;

import jakarta.ws.rs.core.Response;
import tp1.api.service.util.Result;

public class ConvertError {

	public static Response.Status convertError(Result result) {
		switch (result.error()) {
			case BAD_REQUEST -> {
				return Response.Status.BAD_REQUEST;
			}
			case CONFLICT -> {
				return Response.Status.CONFLICT;
			}
			case FORBIDDEN -> {
				return Response.Status.FORBIDDEN;
			}
			case NOT_FOUND -> {
				return Response.Status.NOT_FOUND;
			}
			case OK -> {
				return Response.Status.OK;
			}
			case INTERNAL_ERROR -> {
				return Response.Status.INTERNAL_SERVER_ERROR;
			}
			case NOT_IMPLEMENTED -> {
				return Response.Status.NOT_IMPLEMENTED;
			}
		}

		return null;
	}

}
