grammar Fun;

file : block;

block : (statement)*;

blockWithBraces : '{' block '}';

statement
    : function
    | variable
    | expression
    | whileStatement
    | ifStatement
    | assignment
    | returnStatement
    ;

function : 'fun' IDENTIFIER '(' parameterNames ')' blockWithBraces;

variable : 'var' IDENTIFIER ('=' expression)?;

parameterNames : IDENTIFIER (',' IDENTIFIER)*;

whileStatement : 'while' '(' expression ')' blockWithBraces;

ifStatement : 'if' '(' expression ')' blockWithBraces ('else' blockWithBraces)?;

assignment : IDENTIFIER '=' expression;

returnStatement : 'return' expression;

expression
    : expressionPriority5
    | expressionPriority5 BIN_OP_PRIORITY6 expression
    ;

expressionPriority5
    : expressionPriority4
    | expressionPriority4 BIN_OP_PRIORITY5 expressionPriority5
    ;

expressionPriority4
    : expressionPriority3
    | expressionPriority3 BIN_OP_PRIORITY4 expressionPriority4
    ;

expressionPriority3
    : expressionPriority2
    | expressionPriority2 BIN_OP_PRIORITY3 expressionPriority3
    ;

expressionPriority2
    : expressionPriority1
    | expressionPriority1 BIN_OP_PRIORITY2 expressionPriority2
    ;

expressionPriority1
    : atomicExpression
    | atomicExpression BIN_OP_PRIORITY1 expressionPriority1
    ;

atomicExpression
    : functionCall
    | IDENTIFIER
    | LITERAL
    | '(' expression ')'
    ;

functionCall : IDENTIFIER '(' arguments ')';

arguments : expression (',' expression)*;


BIN_OP_PRIORITY1
    : MUL
    | DIV
    | MOD
    ;

BIN_OP_PRIORITY2
    : PLUS
    | MINUS
    ;

BIN_OP_PRIORITY3
    : LT
    | GT
    | LE
    | GE
    ;

BIN_OP_PRIORITY4
    : EQ
    | NE
    ;

BIN_OP_PRIORITY5
    : AND
    ;

BIN_OP_PRIORITY6
    : OR
    ;


MUL : '*';
DIV : '/';
MOD : '%';

PLUS : '+';
MINUS : '-';

LT : '<';
GT : '>';
LE : '<=';
GE : '>=';

EQ : '==';
NE : '!=';

AND : '&&';

OR : '||';


IDENTIFIER : [_a-zA-Z][_a-zA-Z0-9]*;

LITERAL : '0' | ('1'..'9') + ('0'..'9')*;

COMMENT : '//' ~[\r\n]* -> skip;

WS : (' ' | '\t' | '\r'| '\n') -> skip;
