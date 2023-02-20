package derivapp;
//import java.lang.management.MemoryType;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
import derivapp.IToken.Kind;
public class Lexer implements ILexer {

	
	ArrayList<Token> tokens = new ArrayList<Token>();
	HashMap<String, Kind> keywords = new HashMap<>();
	static final boolean VERBOSE = true;
	int tokenPos = 0;
	int col = 0;
	int pos = 0;
	
	//use Enumerations to increase compile-time checks, as well as list which values are valid
	private enum State {
		START,
		HAVE_VAR,
		HAVE_NUMB,
		HAVE_MINUS,
		HAVE_PLUS,
		HAVE_TIMES,
		HAVE_DIV,
		HAVE_EOF
	}
	
	 
	void show(Object obj) {
		if(VERBOSE) {
			System.out.println(obj);
		}
	}
	
	@Override
	public IToken next() throws LexicalException {
		if(tokens.get(tokenPos).kind == Kind.ERROR) {
			
		}
		return tokens.get(tokenPos++);
	}

	@Override
	public IToken peek() throws LexicalException {
		return tokens.get(tokenPos);
	}
	
	//increment positions
	void inc() 
	{
		pos++;
		this.col++;
	}
	
	public Lexer(String input)
	{
		//establish an EOF token to look for
		input = input + '\0';
		
	 
		//init state with start
		State state = State.START;
		char chars[] = input.toCharArray();
		int line = 0;
		int startPos = 0;
		int startPos2 = 0;
		char ch = chars[pos];
		
		
		//repeat indef until EOF token is found
		while(true) {
			ch = chars[pos];
			
			
			switch(state) {
			//no state entered yet, still checking to see
			case START:
				//increment pos via inc method, then get next character
				//position is increased all the time, startPos2 is set to pos when we return to start state, so it tracks where token begins
//				startPos = col;
				startPos2 = pos;
				ch = chars[pos];
				
				switch(ch) {
				case '(':
					Token lparen_token = new Token(Kind.LPAREN, "(", 0, 0, 1);
					tokens.add(lparen_token);
					inc();
					break;
				case ')':
					Token rparen_token = new Token(Kind.RPAREN, ")", 0, 0, 1);
					tokens.add(rparen_token);
					inc();
					break;
				case 'x':
					Token var_token = new Token(Kind.VAR, "x", line, startPos, 1);
					tokens.add(var_token);
					inc();
					break;
				case '^':
					show("have exp");
					Token exp_token = new Token(Kind.EXP, "^", 0,0,1);
					tokens.add(exp_token);
					inc();
					break;
				case ' ':
					inc();
					break;
				case  '1','2','3','4','5','6','7','8','9':
					state = State.HAVE_NUMB;
					break;
				case '+':
					Token plus_token = new Token(Kind.PLUS, "+", 0,0, 1);
					tokens.add(plus_token);
					inc();
					break;
				case '-':
					Token minus_token = new Token(Kind.MINUS, "+", 0,0, 1);
					tokens.add(minus_token);
					inc();
					break;
				case '*':
					Token times_token = new Token(Kind.TIMES, "+", 0,0, 1);
					tokens.add(times_token);
					inc();
					break;
				case '/':
					Token div_token = new Token(Kind.DIV, "+", 0,0, 1);
					tokens.add(div_token);
					inc();
					break;
				case '\0':
					state = State.HAVE_EOF;
					break;
				}
				break;
			case HAVE_NUMB:
				switch(ch) {
				case '0','1','2','3','4','5','6','7','8','9':
					show("numbr found");
					inc();
					break;	
				case 'x':
					// @ TODO: implement this part with proper line markings for the var_token
					Token var_token = new Token(Kind.VAR, "", 0,0,0);
					tokens.add(var_token);
					inc();
					state = State.START;
					break;
				default:
					Token int_token = new Token(Kind.INT, input.substring(startPos2,pos), 0,0,pos-startPos2);
					try
					{
						int_token.getIntValue();
					}
					catch (Exception e) {
						Token error_token = new Token(Kind.ERROR, "", 0,0,0);
						tokens.add(error_token);
					}
					tokens.add(int_token);
					state= State.START;
					
					break;
				}
				break;
				

			case HAVE_EOF:
				Token EOF_token = new Token(Kind.EOF, "EOF",0,0,1);
				tokens.add(EOF_token);
				return;

				
			
			}
			 
		}
	}


}
