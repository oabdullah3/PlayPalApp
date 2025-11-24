package app.exceptions;

@SuppressWarnings("serial")
public class UserNotFoundException extends RuntimeException{
	public UserNotFoundException(String message) {
        super("UserNotFoundException: " + message);
    }
}
