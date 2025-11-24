package app.exceptions;

@SuppressWarnings("serial")
public class SessionNotFoundException extends RuntimeException{
	public SessionNotFoundException(String message) {
		super("SessionNotFoundException " + message);
	}
}