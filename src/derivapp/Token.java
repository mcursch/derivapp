package derivapp;

public class Token implements IToken{

	final Kind kind;
	final String input;
	final int pos;
	final int line;
	final int length;
	
	public Token(Kind kind, String input, int line, int pos, int length )
	{
		this.line = line;
		this.kind = kind;
		this.input = input;
		this.pos = pos;
		this.length = length;
	}
	
	@Override
	public Kind getKind() {
		// TODO Auto-generated method stub
		return kind;
	}
	@Override
	public String getText() {
		return input.substring(0, length);
	}
	@Override
	public SourceLocation getSourceLocation() {
		SourceLocation source = new SourceLocation(this.line, this.pos);
		return source;
	}
	@Override
	public int getIntValue() {
		return Integer.parseInt(this.getText());
	}
	@Override
	public float getFloatValue() {
		return Float.parseFloat(this.getText());
	}
	@Override
	public String getStringValue() {
		// TODO Auto-generated method stub
		return null;
	}
}
