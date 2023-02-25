package derivapp;

import derivapp.IToken.SourceLocation;

@SuppressWarnings("serial")
public class SyntaxException extends DAException {

	public SyntaxException(String error_message, SourceLocation loc) {
		super(error_message, loc);
		
	}

	public SyntaxException(String message) {
		super(message);
	}
	
	

}
