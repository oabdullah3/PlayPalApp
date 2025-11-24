package app.exceptions;

@SuppressWarnings("serial")
public class SessionFullException extends RuntimeException{
	public SessionFullException(String message) {
		super("SessionFullException " + message);
	}
}