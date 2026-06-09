package derivapp.ast;

import derivapp.IToken;

public class ExponentialExpr extends Expr {

	final Expr base;
	final Expr exponent;

	public ExponentialExpr(IToken firstToken, Expr base, Expr exponent) {
		super(firstToken);
		this.base = base;
		this.exponent = exponent;
	}

	public Expr getBase() {
		return base;
	}

	public Expr getExponent() {
		return exponent;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitExponentialExpr(this, arg);
	}

	@Override
	public String toString() {
		return "ExponentialExpr [base=" + base + ", exponent=" + exponent + "]";
	}

}
