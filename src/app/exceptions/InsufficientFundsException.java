package app.exceptions;

@SuppressWarnings("serial")
public class InsufficientFundsException extends RuntimeException{
	public InsufficientFundsException(String message) {
		super("InsufficientFundsException " + message);
	}
}
