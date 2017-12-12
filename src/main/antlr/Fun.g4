grammar Fun;

file : block;

block : (statement)*;

blockWithBraces : '{' block '}';

statement
    : function
    | variable
    | expressionStatement
    | whileStatement
    | ifStatement
    | assignment
    | returnStatement
    ;

function : 'fun' IDENTIFIER '(' parameterNames ')' blockWithBraces;

variable : 'var' IDENTIFIER ('=' expression)?;

expressionStatement : expression;

parameterNames : (IDENTIFIER (',' IDENTIFIER)*)?;

whileStatement : 'while' '(' expression ')' blockWithBraces;

ifStatement : 'if' '(' expression ')' blockWithBraces ('else' blockWithBraces)?;

assignment : IDENTIFIER '=' expression;

returnStatement : 'return' expression;

expression
    : '-' expression                                        #ExprUnaryMinus
    | '+' expression                                        #ExprUnaryPlus
    | atomicExpression                                      #AtomicExpr
    | expression op=('*' | '/' | '%') expression            #ExprPriority1
    | expression op=('+' | '-') expression                  #ExprPriority2
    | expression op=('<=' | '>=' | '<' | '>') expression    #ExprPriority3
    | expression op=('==' | '!=') expression                #ExprPriority4
    | expression op='&&' expression                         #ExprPriority5
    | expression op='||' expression                         #ExprPriority6
    ;

atomicExpression
    : functionCall
    | identifierExpression
    | literalExpression
    | bracketedExpression
    ;

functionCall : IDENTIFIER '(' arguments ')';

identifierExpression : IDENTIFIER;

bracketedExpression : '(' expression ')';

literalExpression : LITERAL;

arguments : (expression (',' expression)*)?;

IDENTIFIER : [_a-zA-Z][_a-zA-Z0-9]*;

LITERAL : '0' | ('1'..'9') + ('0'..'9')*;

COMMENT : '//' ~[\r\n]* -> skip;

WS : (' ' | '\t' | '\r'| '\n') -> skip;
