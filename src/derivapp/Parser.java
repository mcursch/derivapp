package derivapp;
import derivapp.IToken.Kind;

public class Parser implements IParser{

	public ASTNode parse() throws PLCException{
		 
		return program();
		 
		
			 
	}
	 
	void consume() throws LexicalException
	{
		 
		t = lexer.next();
		 
	}
	void match(Kind kind) throws PLCException
	{
		 
		if(t.getKind() == kind) {
			t = lexer.next();
		}
		else {
			 
			throw new SyntaxException("Error");
		}
	}
	
	public Parser(String input)  {
	  lexer = new Lexer(input);
	  t = lexer.tokens.get(0);
	  
	   
	 
	
}
}
