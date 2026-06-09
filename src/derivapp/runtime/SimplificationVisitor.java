package derivapp.runtime;

import java.util.ArrayList;
import java.util.List;

import derivapp.IToken;
import derivapp.IToken.Kind;
import derivapp.Token;
import derivapp.ast.ASTNode;
import derivapp.ast.ASTVisitor;
import derivapp.ast.BinaryExpr;
import derivapp.ast.ExponentialExpr;
import derivapp.ast.FloatLitExpr;
import derivapp.ast.IdentExpr;
import derivapp.ast.IntLitExpr;
import derivapp.ast.Program;
import derivapp.ast.Expr;
import derivapp.ast.UnaryExpr;

/**
 * A lightweight AST simplifier that eliminates trivial constant-folding cases
 * so that derivative output stays readable.
 *
 * <p>Rules handled:
 * <ul>
 *   <li>{@code 0 + e  →  e}</li>
 *   <li>{@code e + 0  →  e}</li>
 *   <li>{@code e - 0  →  e}</li>
 *   <li>{@code 0 - e  →  -e}</li>
 *   <li>{@code 1 * e  →  e}</li>
 *   <li>{@code e * 1  →  e}</li>
 *   <li>{@code 0 * e  →  0}</li>
 *   <li>{@code e * 0  →  0}</li>
 *   <li>{@code 0 / e  →  0}</li>
 *   <li>{@code e ^ 1  →  e}</li>
 *   <li>{@code e ^ 0  →  1}</li>
 *   <li>{@code -(-e)  →  e}</li>
 *   <li>Integer constant folding for +, -, *, ^ when both operands are int literals.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 *   SimplificationVisitor sv = new SimplificationVisitor();
 *   Program simplified = sv.simplify(program);
 * }</pre>
 */
public class SimplificationVisitor implements ASTVisitor {

    // -----------------------------------------------------------------------
    // Public entry point
    // -----------------------------------------------------------------------

    /**
     * Simplifies every top-level expression in the program and returns a new Program.
     */
    public Program simplify(Program program) throws Exception {
        return (Program) program.visit(this, null);
    }

    // -----------------------------------------------------------------------
    // ASTVisitor implementation
    // -----------------------------------------------------------------------

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        List<ASTNode> simplified = new ArrayList<>();
        for (ASTNode node : program.getEquation()) {
            simplified.add((ASTNode) node.visit(this, arg));
        }
        return new Program(program.getFirstToken(), simplified);
    }

    /** Literals are already in simplest form. */
    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        return intLitExpr;
    }

    /** Literals are already in simplest form. */
    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        return floatLitExpr;
    }

    /** Identifiers are already in simplest form. */
    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        return identExpr;
    }

    /**
     * -(-e) → e
     * -(0)  → 0
     * Otherwise simplify inner expression and rebuild.
     */
    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
        Expr inner = (Expr) unaryExpr.getExpr().visit(this, arg);
        Kind op = unaryExpr.getOp().getKind();

        if (op == Kind.MINUS) {
            if (isZero(inner)) {
                return makeIntLit(0);
            }
            // -(-e) → e
            if (inner instanceof UnaryExpr innerUnary
                    && innerUnary.getOp().getKind() == Kind.MINUS) {
                return innerUnary.getExpr();
            }
            // constant folding for unary minus on int literal
            if (inner instanceof IntLitExpr ile) {
                return makeIntLit(-ile.getValue());
            }
        }

        IToken opTok = unaryExpr.getOp();
        return new UnaryExpr(opTok, opTok, inner);
    }

    /**
     * Applies constant-folding and identity simplifications for binary operators.
     */
    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        Expr left  = (Expr) binaryExpr.getLeft().visit(this, arg);
        Expr right = (Expr) binaryExpr.getRight().visit(this, arg);
        Kind op    = binaryExpr.getOp().getKind();
        IToken opTok = binaryExpr.getOp();

        switch (op) {
            case PLUS -> {
                // 0 + e → e
                if (isZero(left))  return right;
                // e + 0 → e
                if (isZero(right)) return left;
                // constant folding
                if (left instanceof IntLitExpr l && right instanceof IntLitExpr r) {
                    return makeIntLit(l.getValue() + r.getValue());
                }
            }
            case MINUS -> {
                // e - 0 → e
                if (isZero(right)) return left;
                // 0 - e → -e  (only useful when e is non-trivial)
                if (isZero(left)) {
                    if (right instanceof IntLitExpr r) {
                        return makeIntLit(-r.getValue());
                    }
                    IToken minusTok = makeSyntheticToken(Kind.MINUS, "-");
                    return new UnaryExpr(minusTok, minusTok, right);
                }
                // constant folding
                if (left instanceof IntLitExpr l && right instanceof IntLitExpr r) {
                    return makeIntLit(l.getValue() - r.getValue());
                }
            }
            case TIMES -> {
                // 0 * e → 0  and  e * 0 → 0
                if (isZero(left) || isZero(right)) return makeIntLit(0);
                // 1 * e → e  and  e * 1 → e
                if (isOne(left))  return right;
                if (isOne(right)) return left;
                // constant folding
                if (left instanceof IntLitExpr l && right instanceof IntLitExpr r) {
                    return makeIntLit(l.getValue() * r.getValue());
                }
            }
            case DIV -> {
                // 0 / e → 0  (assuming e != 0; we don't check at simplification time)
                if (isZero(left)) return makeIntLit(0);
                // e / 1 → e
                if (isOne(right)) return left;
                // constant folding for integer division
                if (left instanceof IntLitExpr l && right instanceof IntLitExpr r
                        && r.getValue() != 0 && l.getValue() % r.getValue() == 0) {
                    return makeIntLit(l.getValue() / r.getValue());
                }
            }
            default -> { /* no simplification for other ops */ }
        }

        return new BinaryExpr(opTok, left, opTok, right);
    }

    /**
     * e ^ 0 → 1
     * e ^ 1 → e
     * constant folding for integer base and integer exponent.
     */
    @Override
    public Object visitExponentialExpr(ExponentialExpr expExpr, Object arg) throws Exception {
        Expr base     = (Expr) expExpr.getBase().visit(this, arg);
        Expr exponent = (Expr) expExpr.getExponent().visit(this, arg);
        IToken expTok = expExpr.getFirstToken();

        // e ^ 0 → 1
        if (isZero(exponent)) return makeIntLit(1);
        // e ^ 1 → e
        if (isOne(exponent))  return base;

        // constant folding: int^int (non-negative exponent only)
        if (base instanceof IntLitExpr b && exponent instanceof IntLitExpr e) {
            int exp = e.getValue();
            if (exp >= 0) {
                int result = (int) Math.pow(b.getValue(), exp);
                return makeIntLit(result);
            }
        }

        return new ExponentialExpr(expTok, base, exponent);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private IToken makeSyntheticToken(Kind kind, String text) {
        return new Token(kind, text, 0, 0, text.length());
    }

    private IntLitExpr makeIntLit(int value) {
        String text = Integer.toString(value);
        IToken tok = makeSyntheticToken(Kind.INT, text);
        return new IntLitExpr(tok);
    }

    private boolean isZero(Expr e) {
        return e instanceof IntLitExpr ile && ile.getValue() == 0;
    }

    private boolean isOne(Expr e) {
        return e instanceof IntLitExpr ile && ile.getValue() == 1;
    }
}
