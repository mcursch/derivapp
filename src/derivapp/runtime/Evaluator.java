package derivapp.runtime;

import java.util.Map;

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
 * Evaluates a parsed mathematical expression AST.
 *
 * Usage:
 *   Map<String, Double> env = Map.of("x", 3.0);
 *   Evaluator ev = new Evaluator(env);
 *   double result = ev.evaluate(program);
 */
public class Evaluator implements ASTVisitor {

    private final Map<String, Double> env;

    /**
     * @param env variable bindings, e.g. {"x": 3.0}
     */
    public Evaluator(Map<String, Double> env) {
        this.env = env;
    }

    /**
     * Entry point: evaluate a fully-parsed Program node.
     *
     * The Program holds a list of top-level expressions.  For a single
     * expression program the list has exactly one element and its value is
     * returned.  For multi-expression programs the value of the last
     * expression is returned.
     */
    public double evaluate(Program program) throws Exception {
        return (Double) program.visit(this, null);
    }

    // -----------------------------------------------------------------------
    // ASTVisitor implementation
    // -----------------------------------------------------------------------

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        double result = 0.0;
        for (ASTNode node : program.getEquation()) {
            result = (Double) node.visit(this, arg);
        }
        return result;
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        return (double) intLitExpr.getValue();
    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        return (double) floatLitExpr.getValue();
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        String name = identExpr.getText();
        if (!env.containsKey(name)) {
            throw new DARuntimeException("Undefined variable: " + name);
        }
        return env.get(name);
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        double left  = (Double) binaryExpr.getLeft().visit(this, arg);
        double right = (Double) binaryExpr.getRight().visit(this, arg);
        Kind op = binaryExpr.getOp().getKind();

        if (op == Kind.PLUS)  return left + right;
        if (op == Kind.MINUS) return left - right;
        if (op == Kind.TIMES) return left * right;
        if (op == Kind.DIV) {
            if (right == 0.0) throw new DARuntimeException("Division by zero");
            return left / right;
        }
        throw new DARuntimeException("Unsupported binary operator: " + binaryExpr.getOp().getText());
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
        double operand = (Double) unaryExpr.getExpr().visit(this, arg);
        Kind op = unaryExpr.getOp().getKind();

        if (op == Kind.MINUS) return -operand;
        if (op == Kind.PLUS)  return operand;
        throw new DARuntimeException("Unsupported unary operator: " + unaryExpr.getOp().getText());
    }

    @Override
    public Object visitExponentialExpr(ExponentialExpr expExpr, Object arg) throws Exception {
        double base     = (Double) expExpr.getBase().visit(this, arg);
        double exponent = (Double) expExpr.getExponent().visit(this, arg);
        return Math.pow(base, exponent);
    }
}
