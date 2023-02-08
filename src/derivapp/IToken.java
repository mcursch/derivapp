package derivapp;

public interface IToken {

	public record SourceLocation(int line, int column) {}
	
	public static enum Kind {
		VAR, //x, y, z, n,  
		PAREN, //()
		PLUS,	// +
		MINUS,	// -
		DIV,	// /
		TIMES, //*
		INT,   // 1, 2, 3, 4
		ERROR,
		EOF
	}
	
	public Kind getKind();
	public String getText();
	public SourceLocation getSourceLocation();
	public int getIntValue();
	public float getFloatValue();
	public String getStringValue();
}
