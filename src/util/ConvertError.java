package util;

import jakarta.ws.rs.core.Response;
import tp1.api.service.util.Result;

@SuppressWarnings("rawtypes")
public class ConvertError {

	public static Response.Status resultErrorToWebAppError(Result result) {
		return switch (result.error()) {
			case BAD_REQUEST -> Response.Status.BAD_REQUEST;
			case CONFLICT -> Response.Status.CONFLICT;
			case FORBIDDEN -> Response.Status.FORBIDDEN;
			case NOT_FOUND -> Response.Status.NOT_FOUND;
			case OK -> Response.Status.OK;
			case INTERNAL_ERROR ->  Response.Status.INTERNAL_SERVER_ERROR;
			case NOT_IMPLEMENTED -> Response.Status.NOT_IMPLEMENTED;
		};
	}

	public static Result webAppErrorToResultError(Response.Status status) {
		return switch (status) {
			case BAD_REQUEST -> Result.error(Result.ErrorCode.BAD_REQUEST);
			case CONFLICT -> Result.error(Result.ErrorCode.CONFLICT);
			case FORBIDDEN -> Result.error(Result.ErrorCode.FORBIDDEN);
			case NOT_FOUND -> Result.error(Result.ErrorCode.NOT_FOUND);
			case NO_CONTENT -> Result.ok();
			case INTERNAL_SERVER_ERROR -> Result.error(Result.ErrorCode.INTERNAL_ERROR);
			case NOT_IMPLEMENTED -> Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
			default -> null;
		};
	}

}
