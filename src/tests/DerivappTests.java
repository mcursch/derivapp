package tests;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
//
import org.junit.jupiter.api.Test;
//
import derivapp.CompilerComponentFactory;
import derivapp.ILexer;
import derivapp.IToken;
import derivapp.IToken.Kind;
import derivapp.LexicalException;
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
	
	
	//TestDescription
		@Test
		void testOne() throws LexicalException {
//			assertEquals("hello", "hello");
			String input = "x+2";
			show(input);
			ILexer lexer = getLexer(input);
			
			checkToken(lexer.next(), Kind.VAR);
			checkToken(lexer.next(), Kind.PLUS);
			checkToken(lexer.next(), Kind.INT);

//			checkEOF(lexer.next());
		}
		//TestDescription
		@Test
		void testTwo() throws LexicalException {
//			assertEquals("hello", "hello");
			String input = "7x+3";
			show(input);
			ILexer lexer = getLexer(input);
			

			checkToken(lexer.next(), Kind.VAR);
			checkToken(lexer.next(), Kind.PLUS);
			checkToken(lexer.next(), Kind.INT);

//			checkEOF(lexer.next());
		}		//TestDescription
		@Test
		void testThree() throws LexicalException {
//			assertEquals("hello", "hello");
			String input = "x^2";
			show(input);
			ILexer lexer = getLexer(input);
			

			checkToken(lexer.next(), Kind.VAR);
			checkToken(lexer.next(), Kind.EXP);
			checkToken(lexer.next(), Kind.INT);

//			checkEOF(lexer.next());
		}
		
		@Test
		void testFour() throws LexicalException {
//			assertEquals("hello", "hello");
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
			

//			checkEOF(lexer.next());
		}
		
		
		@Test
		void testFive() throws LexicalException {
//			assertEquals("hello", "hello");
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
			

//			checkEOF(lexer.next());
		}
	 
}
