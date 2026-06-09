package tests;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
//
import org.junit.jupiter.api.Test;
//
import derivapp.CompilerComponentFactory;
import derivapp.DAException;
import derivapp.ILexer;
import derivapp.IParser;
import derivapp.IToken;
import derivapp.IToken.Kind;
import derivapp.LexicalException;
import derivapp.SyntaxException;
import derivapp.ast.ASTNode;
import derivapp.ast.BinaryExpr;
import derivapp.ast.ExponentialExpr;
import derivapp.ast.IdentExpr;
import derivapp.ast.IntLitExpr;
import derivapp.ast.Program;
import derivapp.ast.UnaryExpr;
import derivapp.runtime.DARuntimeException;
import derivapp.runtime.DerivativeVisitor;
import derivapp.runtime.Evaluator;
import derivapp.runtime.EvaluatorFactory;
import derivapp.runtime.SimplificationVisitor;

public class DerivappTests {

	static final boolean VERBOSE = true;
	void show(Object obj) {
		if(VERBOSE) {
			System.out.println(obj);
		}
	}

	ILexer getLexer(String input) {
		return CompilerComponentFactory.getLexer(input);
	}

	void checkToken(IToken t, Kind expectedKind) {
		assertEquals(expectedKind, t.getKind());
	}

	/** Helper: parse an input string and return the first expression node from the Program. */
	ASTNode parseFirst(String input) throws DAException {
		IParser parser = CompilerComponentFactory.getParser(input);
		Program program = (Program) parser.parse();
		return program.getEquation().get(0);
	}

	/** Helper: parse an input string and return the full Program. */
	Program parseProgram(String input) throws DAException {
		IParser parser = CompilerComponentFactory.getParser(input);
		return (Program) parser.parse();
	}

	/** Helper: evaluate a parsed program with a variable environment. */
	double evalProgram(Program program, Map<String, Double> env) throws Exception {
		Evaluator ev = EvaluatorFactory.getEvaluator(env);
		return ev.evaluate(program);
	}

	/** Helper: parse, differentiate wrt "x", simplify, then evaluate. */
	double diffAndEval(String input, Map<String, Double> env) throws Exception {
		Program program = parseProgram(input);
		DerivativeVisitor dv = new DerivativeVisitor("x");
		Program derivative = dv.differentiate(program);
		SimplificationVisitor sv = new SimplificationVisitor();
		Program simplified = sv.simplify(derivative);
		show("d/dx(" + input + ") simplified = " + simplified.getEquation());
		Evaluator ev = EvaluatorFactory.getEvaluator(env);
		return ev.evaluate(simplified);
	}

	// -----------------------------------------------------------------------
	// Existing lexer tests
	// -----------------------------------------------------------------------

	//TestDescription
	@Test
	void testOne() throws LexicalException {
		String input = "x+2";
		show(input);
		ILexer lexer = getLexer(input);

		checkToken(lexer.next(), Kind.VAR);
		checkToken(lexer.next(), Kind.PLUS);
		checkToken(lexer.next(), Kind.INT);
	}

	//TestDescription
	@Test
	void testTwo() throws LexicalException {
		String input = "7x+3";
		show(input);
		ILexer lexer = getLexer(input);

		checkToken(lexer.next(), Kind.VAR);
		checkToken(lexer.next(), Kind.PLUS);
		checkToken(lexer.next(), Kind.INT);
	}

	//TestDescription
	@Test
	void testThree() throws LexicalException {
		String input = "x^2";
		show(input);
		ILexer lexer = getLexer(input);

		checkToken(lexer.next(), Kind.VAR);
		checkToken(lexer.next(), Kind.EXP);
		checkToken(lexer.next(), Kind.INT);
	}

	@Test
	void testFour() throws LexicalException {
		String input = "x^2 + 2x - 3";
		show(input);
		ILexer lexer = getLexer(input);

		checkToken(lexer.next(), Kind.VAR);
		checkToken(lexer.next(), Kind.EXP);
		checkToken(lexer.next(), Kind.INT);
		checkToken(lexer.next(), Kind.PLUS);
		checkToken(lexer.next(), Kind.VAR);
		checkToken(lexer.next(), Kind.MINUS);
		checkToken(lexer.next(), Kind.INT);
		checkToken(lexer.next(), Kind.EOF);
	}

	@Test
	void testFive() throws LexicalException {
		String input = "(x+3) + (2x-3)";
		show(input);
		ILexer lexer = getLexer(input);

		checkToken(lexer.next(), Kind.LPAREN);
		checkToken(lexer.next(), Kind.VAR);
		checkToken(lexer.next(), Kind.PLUS);
		checkToken(lexer.next(), Kind.INT);
		checkToken(lexer.next(), Kind.RPAREN);
		checkToken(lexer.next(), Kind.PLUS);
		checkToken(lexer.next(), Kind.LPAREN);
		checkToken(lexer.next(), Kind.VAR);
		checkToken(lexer.next(), Kind.MINUS);
		checkToken(lexer.next(), Kind.INT);
		checkToken(lexer.next(), Kind.RPAREN);
	}

	// -----------------------------------------------------------------------
	// Parser tests
	// -----------------------------------------------------------------------

	@Test
	void testParseIdentExpr() throws DAException {
		show("Parser: \"x\" -> IdentExpr");
		ASTNode node = parseFirst("x");
		assertInstanceOf(IdentExpr.class, node);
		assertEquals("x", node.getText());
	}

	@Test
	void testParseIntLitExpr() throws DAException {
		show("Parser: \"42\" -> IntLitExpr with value 42");
		ASTNode node = parseFirst("42");
		assertInstanceOf(IntLitExpr.class, node);
		assertEquals(42, ((IntLitExpr) node).getValue());
	}

	@Test
	void testParseBinaryExprPlus() throws DAException {
		show("Parser: \"x+1\" -> BinaryExpr with PLUS");
		ASTNode node = parseFirst("x+1");
		assertInstanceOf(BinaryExpr.class, node);
		BinaryExpr bin = (BinaryExpr) node;
		assertEquals(Kind.PLUS, bin.getOp().getKind());
		assertInstanceOf(IdentExpr.class, bin.getLeft());
		assertInstanceOf(IntLitExpr.class, bin.getRight());
	}

	@Test
	void testParseBinaryExprTimes() throws DAException {
		show("Parser: \"x*x\" -> BinaryExpr with TIMES");
		ASTNode node = parseFirst("x*x");
		assertInstanceOf(BinaryExpr.class, node);
		BinaryExpr bin = (BinaryExpr) node;
		assertEquals(Kind.TIMES, bin.getOp().getKind());
		assertInstanceOf(IdentExpr.class, bin.getLeft());
		assertInstanceOf(IdentExpr.class, bin.getRight());
	}

	@Test
	void testParseNestedBinaryExpr() throws DAException {
		show("Parser: \"(x+1)*(x+3)\" -> nested BinaryExpr");
		ASTNode node = parseFirst("(x+1)*(x+3)");
		assertInstanceOf(BinaryExpr.class, node);
		BinaryExpr outer = (BinaryExpr) node;
		assertEquals(Kind.TIMES, outer.getOp().getKind());
		assertInstanceOf(BinaryExpr.class, outer.getLeft());
		assertInstanceOf(BinaryExpr.class, outer.getRight());
		BinaryExpr left = (BinaryExpr) outer.getLeft();
		BinaryExpr right = (BinaryExpr) outer.getRight();
		assertEquals(Kind.PLUS, left.getOp().getKind());
		assertEquals(Kind.PLUS, right.getOp().getKind());
	}

	@Test
	void testParseExponentialExpr() throws DAException {
		show("Parser: \"x^2\" -> ExponentialExpr");
		ASTNode node = parseFirst("x^2");
		assertInstanceOf(ExponentialExpr.class, node);
		ExponentialExpr exp = (ExponentialExpr) node;
		assertInstanceOf(IdentExpr.class, exp.getBase());
		assertInstanceOf(IntLitExpr.class, exp.getExponent());
		assertEquals(2, ((IntLitExpr) exp.getExponent()).getValue());
	}

	@Test
	void testParseUnaryMinusExpr() throws DAException {
		show("Parser: \"-x\" -> UnaryExpr with MINUS");
		ASTNode node = parseFirst("-x");
		assertInstanceOf(UnaryExpr.class, node);
		UnaryExpr unary = (UnaryExpr) node;
		assertEquals(Kind.MINUS, unary.getOp().getKind());
		assertInstanceOf(IdentExpr.class, unary.getExpr());
	}

	@Test
	void testParseInvalidInputThrowsSyntaxException() {
		show("Parser: invalid input -> SyntaxException");
		assertThrows(SyntaxException.class, () -> {
			parseFirst("(x+");
		});
	}

	// -----------------------------------------------------------------------
	// Evaluator tests
	// -----------------------------------------------------------------------

	@Test
	void testEvaluateConstant() throws Exception {
		show("Evaluator: \"3\" -> 3.0");
		Program program = parseProgram("3");
		double result = evalProgram(program, Map.of());
		assertEquals(3.0, result, 1e-9);
	}

	@Test
	void testEvaluateVariable() throws Exception {
		show("Evaluator: \"x\" with x=5 -> 5.0");
		Program program = parseProgram("x");
		double result = evalProgram(program, Map.of("x", 5.0));
		assertEquals(5.0, result, 1e-9);
	}

	@Test
	void testEvaluateAddition() throws Exception {
		show("Evaluator: \"x+2\" with x=3 -> 5.0");
		Program program = parseProgram("x+2");
		double result = evalProgram(program, Map.of("x", 3.0));
		assertEquals(5.0, result, 1e-9);
	}

	@Test
	void testEvaluateMultiplication() throws Exception {
		show("Evaluator: \"x*x\" with x=4 -> 16.0");
		Program program = parseProgram("x*x");
		double result = evalProgram(program, Map.of("x", 4.0));
		assertEquals(16.0, result, 1e-9);
	}

	@Test
	void testEvaluateExponentiation() throws Exception {
		show("Evaluator: \"x^2\" with x=3 -> 9.0");
		Program program = parseProgram("x^2");
		double result = evalProgram(program, Map.of("x", 3.0));
		assertEquals(9.0, result, 1e-9);
	}

	@Test
	void testEvaluateNestedBinaryExpr() throws Exception {
		show("Evaluator: \"(x+1)*(x+3)\" with x=2 -> 15.0");
		Program program = parseProgram("(x+1)*(x+3)");
		double result = evalProgram(program, Map.of("x", 2.0));
		assertEquals(15.0, result, 1e-9);
	}

	@Test
	void testEvaluateDivision() throws Exception {
		show("Evaluator: \"x/2\" with x=6 -> 3.0");
		Program program = parseProgram("x/2");
		double result = evalProgram(program, Map.of("x", 6.0));
		assertEquals(3.0, result, 1e-9);
	}

	@Test
	void testEvaluateUndefinedVariableThrowsDARuntimeException() throws DAException {
		show("Evaluator: undefined variable -> DARuntimeException");
		// Parse "x" but provide no bindings so x is undefined at evaluation time
		Program program = parseProgram("x");
		Evaluator ev = EvaluatorFactory.getEvaluator(Map.of());
		assertThrows(DARuntimeException.class, () -> {
			ev.evaluate(program);
		});
	}

	@Test
	void testEvaluateDivisionByZeroThrowsDARuntimeException() throws DAException {
		show("Evaluator: division by zero -> DARuntimeException");
		// Evaluate 1/x with x=0.0 to trigger the division-by-zero runtime check
		Program program = parseProgram("1/x");
		Evaluator ev = EvaluatorFactory.getEvaluator(Map.of("x", 0.0));
		assertThrows(DARuntimeException.class, () -> {
			ev.evaluate(program);
		});
	}

	// -----------------------------------------------------------------------
	// Derivative tests
	// -----------------------------------------------------------------------

	@Test
	void testDerivativeOfConstantIsZero() throws Exception {
		show("d/dx(3) = 0");
		double result = diffAndEval("3", Map.of());
		assertEquals(0.0, result, 1e-9);
	}

	@Test
	void testDerivativeOfVariableIsOne() throws Exception {
		show("d/dx(x) = 1");
		double result = diffAndEval("x", Map.of("x", 0.0));
		assertEquals(1.0, result, 1e-9);
	}

	@Test
	void testDerivativeOfXPlusConstant() throws Exception {
		show("d/dx(x+3) = 1");
		// After differentiation and simplification: 1+0 -> 1
		double result = diffAndEval("x+3", Map.of());
		assertEquals(1.0, result, 1e-9);
	}

	@Test
	void testDerivativeOfXTimesX() throws Exception {
		show("d/dx(x*x) = 2x, evaluate at x=3 should give 6.0");
		// d/dx(x*x) = 1*x + x*1 = x + x = 2x
		double result = diffAndEval("x*x", Map.of("x", 3.0));
		assertEquals(6.0, result, 1e-9);
	}

	@Test
	void testDerivativeOfXSquared() throws Exception {
		show("d/dx(x^2) = 2x, evaluate at x=3 should give 6.0");
		// Power rule: d/dx(x^2) = 2 * x^(2-1) * 1 = 2x
		double result = diffAndEval("x^2", Map.of("x", 3.0));
		assertEquals(6.0, result, 1e-9);
	}

	@Test
	void testDerivativeOfXCubed() throws Exception {
		show("d/dx(x^3) = 3x^2, evaluate at x=2 should give 12.0");
		// Power rule: d/dx(x^3) = 3 * x^(3-1) * 1 = 3*x^2
		double result = diffAndEval("x^3", Map.of("x", 2.0));
		assertEquals(12.0, result, 1e-9);
	}

	@Test
	void testDerivativeOfProductRule() throws Exception {
		show("d/dx((x+1)*(x+3)) = (x+3)+(x+1), evaluate at x=2 should give 8.0");
		// Product rule: d/dx((x+1)*(x+3)) = 1*(x+3) + (x+1)*1 = (x+3) + (x+1)
		// At x=2: (2+3) + (2+1) = 5 + 3 = 8
		double result = diffAndEval("(x+1)*(x+3)", Map.of("x", 2.0));
		assertEquals(8.0, result, 1e-9);
	}

	@Test
	void testDerivativeOfXDividedByTwo() throws Exception {
		show("d/dx(x/2) = 1/2 = 0.5");
		// Quotient rule: d/dx(x/2) = (1*2 - x*0) / 2^2 = 2/4 = 0.5
		// After simplification this reduces to 0.5
		double result = diffAndEval("x/2", Map.of("x", 0.0));
		assertEquals(0.5, result, 1e-9);
	}

}
