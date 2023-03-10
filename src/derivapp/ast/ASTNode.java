package derivapp.ast;

import derivapp.IToken;
import derivapp.IToken.SourceLocation;

public abstract class ASTNode  {
	

	final IToken firstToken;

	public ASTNode(IToken firstToken) {
		this.firstToken = firstToken;
	}

	public SourceLocation getSourceLoc() {
		return firstToken.getSourceLocation();
	}

	public String getText() {
		return firstToken.getText();
	}
	
	public IToken getFirstToken() {
		return firstToken;
	}
	
	public abstract Object visit(ASTVisitor v, Object arg) throws  Exception;

	
}
