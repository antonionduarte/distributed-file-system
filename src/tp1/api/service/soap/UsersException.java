package tp1.api.service.soap;

import jakarta.xml.ws.WebFault;

import java.io.Serial;

@WebFault
public class UsersException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	public UsersException(String errorMessage) {
		super(errorMessage);
	}
}
