package derivapp.ast;

import java.util.List;

import derivapp.IToken;

public class Program extends ASTNode {	
	
	final String name; 
	final List<ASTNode> decsAndStatements;

	public Program(IToken firstToken , String name) {
		super(firstToken);
		this.returnType = returnType;
		this.name = name;
		this.params = params;
		this.decsAndStatements = decsAndStatements;
	}

	public Type getReturnType() {
		return returnType;
	}

	public String getName() {
		return name;
	}

	public List<NameDef> getParams() {
		return params;
	}

	public List<ASTNode> getDecsAndStatements() {
		return decsAndStatements;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitProgram(this, arg);
	}

	@Override
	public String toString() {
		return "Program [returnType=" + returnType + ", name=" + name + ", params=" + params + ", decsAndStatements="
				+ decsAndStatements + "]";
	}

}
