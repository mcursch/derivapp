package derivapp;

import derivapp.IToken.SourceLocation;

@SuppressWarnings("serial")
public class LexicalException extends DerivException{
	public LexicalException(String error_message, SourceLocation loc) {
		super(error_message, loc);
	}

	public LexicalException(String message) {
		super(message);
	}

	public LexicalException(Throwable cause) {
		super(cause);
	}
	
	public LexicalException(String error_message, int line, int column) {
		super(line + ":" + column + "  " + error_message);
	}
}
