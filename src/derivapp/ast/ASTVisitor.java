package derivapp.ast;

public interface ASTVisitor {
	
	Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception;

	Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception;

	Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception;

	Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception;

	Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception;

	Object visitProgram(Program program, Object arg) throws Exception;

	Object visitExponentialExpr(ExponentialExpr expExpr, Object arg) throws Exception;
}
