package derivapp.runtime;

import derivapp.IToken.Kind;
import derivapp.ast.ASTNode;
import derivapp.ast.ASTVisitor;
import derivapp.ast.BinaryExpr;
import derivapp.ast.ExponentialExpr;
import derivapp.ast.FloatLitExpr;
import derivapp.ast.IdentExpr;
import derivapp.ast.IntLitExpr;
import derivapp.ast.Program;
import derivapp.ast.UnaryExpr;

/**
 * Converts an AST back to a readable infix math string.
 *
 * Usage:
 *   String s = new PrintVisitor().print(program);
 */
public class PrintVisitor implements ASTVisitor {

    /**
     * Entry point: print the first (and typically only) expression in the program.
     */
    public String print(Program program) throws Exception {
        return (String) program.visit(this, null);
    }

    // -----------------------------------------------------------------------
    // ASTVisitor implementation
    // -----------------------------------------------------------------------

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (ASTNode node : program.getEquation()) {
            sb.append((String) node.visit(this, arg));
        }
        return sb.toString();
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        return Integer.toString(intLitExpr.getValue());
    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        return Float.toString(floatLitExpr.getValue());
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        return identExpr.getText();
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
        String inner = (String) unaryExpr.getExpr().visit(this, arg);
        Kind op = unaryExpr.getOp().getKind();
        if (op == Kind.MINUS) {
            return "-" + inner;
        }
        // unary plus: just return inner
        return inner;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        String left  = (String) binaryExpr.getLeft().visit(this, arg);
        String right = (String) binaryExpr.getRight().visit(this, arg);
        Kind op = binaryExpr.getOp().getKind();

        String opStr = switch (op) {
            case PLUS  -> "+";
            case MINUS -> "-";
            case TIMES -> "*";
            case DIV   -> "/";
            default    -> binaryExpr.getOp().getText();
        };

        return "(" + left + opStr + right + ")";
    }

    @Override
    public Object visitExponentialExpr(ExponentialExpr expExpr, Object arg) throws Exception {
        String base     = (String) expExpr.getBase().visit(this, arg);
        String exponent = (String) expExpr.getExponent().visit(this, arg);
        return "(" + base + "^" + exponent + ")";
    }
}
