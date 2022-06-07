package tp1.api.service.util;

import java.net.URI;

/**
 * Represents the result of an operation, either wrapping a result of the given type, or an error.
 *
 * @param <T> type of the result value associated with success
 * @author smd
 */
public interface Result<T> {

	/**
	 * Convenience method for returning non error results of the given type
	 *
	 * @param result value of the result
	 * @return the value of the result
	 */
	static <T> Result<T> ok(T result) {
		return new OkResult<>(result);
	}

	static <T> Result<T> ok(T result, long version) {
		return new OkResult<>(result, version);
	}

	/**
	 * Convenience method for returning non error results without a value
	 *
	 * @return non-error result
	 */
	static <T> OkResult<T> ok() {
		return new OkResult<>();
	}

	static <T> OkResult<T> ok(URI uriRedirect) {
		return new OkResult<>(uriRedirect);
	}

	static <T> OkResult<T> ok(URI uriRedirect, long version) {
		return new OkResult<>(uriRedirect, version);
	}

	static <T> OkResult<T> ok(Long version) {
		return new OkResult<>(version);
	}

	/**
	 * Convenience method used to return an error
	 *
	 * @return
	 */
	static <T> ErrorResult<T> error(ErrorCode error) {
		return new ErrorResult<>(error);
	}

	/**
	 * Tests if the result is an error.
	 */
	boolean isOK();

	/**
	 * obtains the payload value of this result
	 *
	 * @return the value of this result.
	 */
	T value();

	/**
	 * obtains the provided URI for redirection
	 *
	 * @return URI for redirection
	 */
	URI redirectURI();

	/**
	 * obtains the error code of this result
	 *
	 * @return the error code
	 */
	ErrorCode error();

	long version();


	/**
	 * @author smd
	 * <p>
	 * Service errors: OK - no error, implies a non-null result of type T, except for Void operations CONFLICT -
	 * something is being created but already exists NOT_FOUND - access occurred to something that does not exist
	 * INTERNAL_ERROR - something unexpected happened
	 */
	enum ErrorCode {OK, CONFLICT, NOT_FOUND, BAD_REQUEST, FORBIDDEN, INTERNAL_ERROR, NOT_IMPLEMENTED}
}

/*
 *
 */
class OkResult<T> implements tp1.api.service.util.Result<T> {

	T result;

	URI redirectURI;

	Long version = 0L;

	OkResult() {
	}

	OkResult(T result) {
		this.result = result;
	}

	OkResult(Long version) {
		this.version = version;
	}

	OkResult(URI redirectURI) {
		this.redirectURI = redirectURI;
	}

	OkResult(T result, Long version) {
		this.result = result;
		this.version = version;
	}

	OkResult(URI redirectURI, Long version) {
		this.redirectURI = redirectURI;
		this.version = version;
	}



	@Override
	public boolean isOK() {
		return true;
	}

	@Override
	public T value() {
		return result;
	}

	@Override
	public URI redirectURI() {
		return redirectURI;
	}

	@Override
	public long version() {
		return version;
	}


	@Override
	public ErrorCode error() {
		return ErrorCode.OK;
	}

	public String toString() {
		return "(OK, " + value() + ")";
	}
}

class ErrorResult<T> implements tp1.api.service.util.Result<T> {

	final ErrorCode error;

	ErrorResult(ErrorCode error) {
		this.error = error;
	}

	@Override
	public boolean isOK() {
		return false;
	}

	@Override
	public T value() {
		throw new RuntimeException("Attempting to extract the value of an Error: " + error());
	}

	@Override
	public URI redirectURI() {
		throw new RuntimeException("Attempting to extract the redirect URI of an Error: " + error());
	}

	@Override
	public ErrorCode error() {
		return error;
	}

	@Override
	public long version() {
		return 0;
	}

	public String toString() {
		return "(" + error() + ")";
	}
}