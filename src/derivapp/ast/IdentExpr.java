package derivapp.ast;
import derivapp.IToken;

public class IdentExpr extends Expr {
	
		
	public IdentExpr(IToken firstToken) {
		super(firstToken);
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentExpr(this, arg);
	}

	@Override
	public String toString() {
		return "method toString not fully implemented";
		//should probably return values here
	}


	
}
