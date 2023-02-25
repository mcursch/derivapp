package derivapp.ast;

import java.util.List;

import derivapp.IToken;

public class Program extends ASTNode {	
	
	final List<ASTNode> equation;

	public Program(IToken firstToken, List<ASTNode> equation) {
		super(firstToken);
		this.equation = equation;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitProgram(this, arg);
	}



}
