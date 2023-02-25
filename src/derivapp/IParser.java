package derivapp;



import derivapp.ast.ASTNode;

public interface IParser {
	ASTNode parse() throws DAException;
	
}
