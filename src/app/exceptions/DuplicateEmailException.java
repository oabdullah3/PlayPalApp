package app.exceptions;

@SuppressWarnings("serial")
public class DuplicateEmailException extends RuntimeException{
	public DuplicateEmailException(String message) {
		super("DuplicateEmailException " + message);
	}
}