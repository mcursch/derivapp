package derivapp;

public interface ILexer {
	IToken next() throws LexicalException;
	IToken peek() throws LexicalException;
}
