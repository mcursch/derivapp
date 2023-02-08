package derivapp;

public class CompilerComponentFactory {
	public static ILexer getLexer(String input) {
		return new Lexer(input);
	}
}
