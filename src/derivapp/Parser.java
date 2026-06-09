package derivapp;
import derivapp.IToken.Kind;
import derivapp.ast.ASTNode;
import java.util.ArrayList;
import java.util.List;
import derivapp.ILexer;
import derivapp.ast.*;
public class Parser implements IParser{

	Lexer lexer;
	IToken t;
	
	public ASTNode parse() throws DAException{
		return program();	 
	}
	
	protected boolean isKind(Kind kind) {
		return t.getKind() == kind;
	}
	
	//going to do the main parsin ghere and return this
	public Program program() throws DAException {
		IToken firstToken = t;
		List<ASTNode> equation = new ArrayList<ASTNode>();
		
		for(int i = 0; i < lexer.tokens.size(); i++) {
			
		}
		
		//begin parse
		while(t.getKind() != Kind.EOF)
		{
			equation.add(expr());
		}
		return new Program(firstToken, equation);
	}

	
	public Expr expr() throws DAException {
		return AdditiveExpr();
	}
	
 	
	
	//Additive expr will be base. Represented as 
	//multExpr ( (+ | -) MultExpr)*
	
	public Expr AdditiveExpr() throws DAException {
		IToken firstToken = t;
		Expr left = null;
		Expr right = null;
		
		left = MultiplicativeExpr();
		
		while(isKind(Kind.MINUS) || isKind(Kind.PLUS)) {
			//following this procedure, we'll make nested binary expressions
			IToken op = t;
			consume();
			right = MultiplicativeExpr();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		return left;
	}
	
	//UnaryExpr ((* | / | %) UnaryExpr)*
	public Expr MultiplicativeExpr() throws DAException {
		IToken firstToken = t;
		Expr left = null;
		Expr right = null;
		
		left = UnaryExpr();
		 
		 
		while(isKind(Kind.TIMES) || isKind(Kind.DIV)) {
			 
			IToken op = t;
			consume();
			right = UnaryExpr();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		return left;
	}
	
	//negatives
	public Expr UnaryExpr() throws DAException {
		if(isKind(Kind.MINUS)) {
			IToken firstToken = t;
			IToken op = t;
			consume();

			Expr e = UnaryExpr();
			Expr left = new UnaryExpr(firstToken, op, e);
			return left;
		}
		else
		{
			return ExponentialExpr();
		}
	}

	// base ^ exponent (right-associative)
	public Expr ExponentialExpr() throws DAException {
		IToken firstToken = t;
		Expr base = PrimaryExpr();
		if (isKind(Kind.EXP)) {
			IToken expTok = t;
			consume();
			Expr exponent = UnaryExpr(); // right-associative
			return new derivapp.ast.ExponentialExpr(firstToken, base, exponent);
		}
		return base;
	}
	//gives us a way to loop back around to recursively define expressions

	public Expr PrimaryExpr() throws DAException {
		IToken firstToken = t;
		Expr e = null;
		if(t.getKind() == Kind.LPAREN) {
			consume();
			e = expr();
			match(Kind.RPAREN);
		}
		else if(t.getKind() == Kind.INT) {
			e = new IntLitExpr(firstToken);
			consume();
		}
		else if(t.getKind() == Kind.VAR) {
			e = new IdentExpr(firstToken);
			consume();
		}
		else {
			throw new SyntaxException("Unexpected token: " + t.getText(), t.getSourceLocation());
		}
		return e;
	}
	
	

	void consume() throws LexicalException
	{
		t = lexer.next();
	}
	
	void match(Kind kind) throws DAException 
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
