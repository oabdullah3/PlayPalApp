package app.exceptions;

@SuppressWarnings("serial")
public class BookingFailedException extends RuntimeException{
	public BookingFailedException(String message) {
		super("BookingFailedException " + message);
	}
}