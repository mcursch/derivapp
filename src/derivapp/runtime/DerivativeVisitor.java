package derivapp.runtime;

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
 * Symbolically differentiates a parsed expression AST with respect to a given variable.
 *
 * <p>Rules implemented:
 * <ul>
 *   <li>Constant rule:   d/dx(c) = 0</li>
 *   <li>Variable rule:   d/dx(x) = 1, d/dx(y) = 0 for y != x</li>
 *   <li>Sum/Difference:  d/dx(f ± g) = f' ± g'</li>
 *   <li>Product rule:    d/dx(f * g) = f'g + fg'</li>
 *   <li>Quotient rule:   d/dx(f / g) = (f'g - fg') / g²</li>
 *   <li>Power rule:      d/dx(f^n) = n * f^(n-1) * f'  (general chain-rule form)</li>
 *   <li>Unary negation:  d/dx(-f) = -f'</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 *   DerivativeVisitor dv = new DerivativeVisitor("x");
 *   Program derivative = dv.differentiate(program);
 * }</pre>
 */
public class DerivativeVisitor implements ASTVisitor {

    private final String variableName;

    /**
     * @param variableName the variable to differentiate with respect to (e.g. "x")
     */
    public DerivativeVisitor(String variableName) {
        this.variableName = variableName;
    }

    // -----------------------------------------------------------------------
    // Public entry point
    // -----------------------------------------------------------------------

    /**
     * Differentiates every top-level expression in the program and returns a
     * new Program whose equation list contains the derivatives.
     */
    public Program differentiate(Program program) throws Exception {
        return (Program) program.visit(this, null);
    }

    // -----------------------------------------------------------------------
    // ASTVisitor implementation
    // -----------------------------------------------------------------------

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        java.util.List<ASTNode> derivedNodes = new java.util.ArrayList<>();
        for (ASTNode node : program.getEquation()) {
            Expr derived = (Expr) node.visit(this, arg);
            derivedNodes.add(derived);
        }
        return new Program(program.getFirstToken(), derivedNodes);
    }

    /**
     * Constant rule: d/dx(c) = 0
     */
    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        return makeIntLit(0);
    }

    /**
     * Constant rule: d/dx(c) = 0
     */
    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        return makeIntLit(0);
    }

    /**
     * Variable rule: d/dx(x) = 1, d/dx(y) = 0 for y != x
     */
    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        if (variableName.equals(identExpr.getText())) {
            return makeIntLit(1);
        } else {
            return makeIntLit(0);
        }
    }

    /**
     * Unary negation: d/dx(-f) = -f'
     * Unary plus:     d/dx(+f) = f'
     */
    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
        Expr innerDeriv = (Expr) unaryExpr.getExpr().visit(this, arg);
        Kind op = unaryExpr.getOp().getKind();

        if (op == Kind.MINUS) {
            // -f' — but simplify -(0) to 0
            if (isZero(innerDeriv)) {
                return makeIntLit(0);
            }
            IToken minusTok = makeSyntheticToken(Kind.MINUS, "-");
            return new UnaryExpr(minusTok, minusTok, innerDeriv);
        } else {
            // +f' — same as f'
            return innerDeriv;
        }
    }

    /**
     * Sum/Difference:  d/dx(f ± g) = f' ± g'
     * Product rule:    d/dx(f * g) = f'g + fg'
     * Quotient rule:   d/dx(f / g) = (f'g - fg') / g²
     */
    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        Expr f  = binaryExpr.getLeft();
        Expr g  = binaryExpr.getRight();
        Kind op = binaryExpr.getOp().getKind();

        Expr fPrime = (Expr) f.visit(this, arg);
        Expr gPrime = (Expr) g.visit(this, arg);

        if (op == Kind.PLUS || op == Kind.MINUS) {
            IToken opTok = makeSyntheticToken(op, op == Kind.PLUS ? "+" : "-");
            return makeBinary(fPrime, opTok, gPrime);
        } else if (op == Kind.TIMES) {
            IToken timesTok = makeSyntheticToken(Kind.TIMES, "*");
            IToken plusTok  = makeSyntheticToken(Kind.PLUS,  "+");
            Expr left  = makeBinary(fPrime, timesTok, g);
            Expr right = makeBinary(f,      timesTok, gPrime);
            return makeBinary(left, plusTok, right);
        } else if (op == Kind.DIV) {
            IToken timesTok = makeSyntheticToken(Kind.TIMES, "*");
            IToken minusTok = makeSyntheticToken(Kind.MINUS, "-");
            IToken divTok   = makeSyntheticToken(Kind.DIV,   "/");
            IToken expTok   = makeSyntheticToken(Kind.EXP,   "^");
            Expr numerator = makeBinary(
                    makeBinary(fPrime, timesTok, g),
                    minusTok,
                    makeBinary(f, timesTok, gPrime));
            Expr denominator = new ExponentialExpr(expTok, g, makeIntLit(2));
            return makeBinary(numerator, divTok, denominator);
        }
        throw new DARuntimeException("Unsupported binary operator in derivative: " + binaryExpr.getOp().getText());
    }

    /**
     * Power rule (general, with chain rule):
     *   d/dx(f^n) = n * f^(n-1) * f'
     *
     * If the exponent is a constant and f is the differentiation variable,
     * this simplifies to the classic power rule.
     */
    @Override
    public Object visitExponentialExpr(ExponentialExpr expExpr, Object arg) throws Exception {
        Expr base     = expExpr.getBase();
        Expr exponent = expExpr.getExponent();

        Expr basePrime = (Expr) base.visit(this, arg);

        // If the base derivative is 0, the whole derivative is 0
        if (isZero(basePrime)) {
            return makeIntLit(0);
        }

        // n * f^(n-1) * f'
        IToken expTok   = makeSyntheticToken(Kind.EXP,   "^");
        IToken timesTok = makeSyntheticToken(Kind.TIMES, "*");
        IToken minusTok = makeSyntheticToken(Kind.MINUS, "-");

        // f^(n-1)
        Expr nMinusOne  = makeBinary(exponent, minusTok, makeIntLit(1));
        Expr fPowNMinus1 = new ExponentialExpr(expTok, base, nMinusOne);

        // n * f^(n-1)
        Expr nTimesFPow = makeBinary(exponent, timesTok, fPowNMinus1);

        // n * f^(n-1) * f'
        if (isOne(basePrime)) {
            // Omit the trailing * f' since f' = 1
            return nTimesFPow;
        }
        return makeBinary(nTimesFPow, timesTok, basePrime);
    }

    // -----------------------------------------------------------------------
    // Helper — AST construction utilities
    // -----------------------------------------------------------------------

    /**
     * Creates a synthetic token with the given kind and text.
     * Position info is set to (0, 0) since these nodes are generated, not parsed.
     */
    private IToken makeSyntheticToken(Kind kind, String text) {
        return new Token(kind, text, 0, 0, text.length());
    }

    /**
     * Creates an IntLitExpr node for the given integer value.
     */
    private IntLitExpr makeIntLit(int value) {
        String text = Integer.toString(value);
        IToken tok = makeSyntheticToken(Kind.INT, text);
        return new IntLitExpr(tok);
    }

    /**
     * Creates an IdentExpr node for the given variable name.
     */
    private IdentExpr makeIdent(String name) {
        IToken tok = makeSyntheticToken(Kind.VAR, name);
        return new IdentExpr(tok);
    }

    /**
     * Creates a BinaryExpr.  The firstToken of the new node is the operator token.
     */
    private BinaryExpr makeBinary(Expr left, IToken op, Expr right) {
        return new BinaryExpr(op, left, op, right);
    }

    // -----------------------------------------------------------------------
    // Helper — constant detection for light simplification
    // -----------------------------------------------------------------------

    /** Returns true if the expression is the integer literal 0. */
    private boolean isZero(Expr e) {
        return (e instanceof IntLitExpr) && ((IntLitExpr) e).getValue() == 0;
    }

    private boolean isOne(Expr e) {
        return (e instanceof IntLitExpr) && ((IntLitExpr) e).getValue() == 1;
    }
}
