package util;

import jakarta.ws.rs.core.Response;
import tp1.api.service.util.Result;

@SuppressWarnings("rawtypes")
public class ConvertError {

	public static Response.Status resultErrorToWebAppError(Result result) {
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

	public static Result webAppErrorToResultError(Response.Status status) {
		switch (status) {
			case BAD_REQUEST -> {
				return Result.error(Result.ErrorCode.BAD_REQUEST);
			}
			case CONFLICT -> {
				return Result.error(Result.ErrorCode.CONFLICT);
			}
			case FORBIDDEN -> {
				return Result.error(Result.ErrorCode.FORBIDDEN);
			}
			case NOT_FOUND -> {
				return Result.error(Result.ErrorCode.NOT_FOUND);
			}
			case NO_CONTENT -> {
				return Result.ok();
			}
			case INTERNAL_SERVER_ERROR -> {
				return Result.error(Result.ErrorCode.INTERNAL_ERROR);
			}
			case NOT_IMPLEMENTED -> {
				return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
			}
		}
		return null;
	}

}
