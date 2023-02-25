package derivapp;


public class CompilerComponentFactory {
	public static ILexer getLexer(String input) {
		return new Lexer(input);
	}
	
	public static IParser getParser(String input) {
		return new Parser(input);
	}
}
