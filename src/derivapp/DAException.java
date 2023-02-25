package derivapp;

/**
 * This is the superclass of all the exceptions that will be thrown in our project
 */

import derivapp.IToken.SourceLocation;

@SuppressWarnings("serial")
public class DAException extends Exception {

	public DAException(String message) {
		super(message);
	}

	public DAException(Throwable cause) {
		super(cause);
	}
	
	public DAException(String error_message, int line, int column) {
		super(line + ":" + column + "  " + error_message);
	}

	public DAException(String error_message, SourceLocation loc) {
		super(loc.line()+ ":" + loc.column() + " " + error_message);
	}

}
