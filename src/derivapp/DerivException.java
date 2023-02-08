package derivapp;

import derivapp.IToken.SourceLocation;

@SuppressWarnings("serial")
public class DerivException extends Exception{

	public DerivException(String message) {
		super(message);
	}

	public DerivException(Throwable cause) {
		super(cause);
	}
	
	public DerivException(String error_message, int line, int column) {
		super(line + ":" + column + "  " + error_message);
	}

	public DerivException(String error_message, SourceLocation loc) {
		super(loc.line()+ ":" + loc.column() + " " + error_message);
	}
}
