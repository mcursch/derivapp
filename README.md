Grammar
dont need to add a statement for each line. 

Whole line will only be one statement, one nested expression

(x+1)^2 is an additive expression inside an expo expression

((x+1)*(x+3))/(x+1)

So we can just nest up this expression

will only have one expression in the end returned, then we can manipulate that

MultiExpr:
(UnaryExpr '*' '/' UnaryExpr)*

UnaryExpr:
	('-') (UnaryExpr | ParentheticalExpr)

ParentheticalExpr
	'(' () ')'

-2(2x+7)

multexxpr
unary -> int -> -2
unary -> parenthetical -> ?

exponentialExpr:
(expr)^n whwere n an int

