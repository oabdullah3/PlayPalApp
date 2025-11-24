package app.exceptions;

@SuppressWarnings("serial")
public class InvalidCredentialsException extends RuntimeException{
	public InvalidCredentialsException(String message) {
		super("InvalidCredentialsException " + message);
	}
}
