grammar BlueJay;

program
    : block EOF
    ;

block
    : stat*
    ;

stat
    : assignment
    | compoundAssignment SCOL
    | if_stat
    | while_stat
    | for_stat
    | print
    | incrDecr SCOL
    | OTHER {System.err.println("unknown identifier: " + $OTHER.text);}
    ;

assignment
    : ID ASSIGN expr SCOL
    ;


// if statement
if_stat
    : IF if_condition_block (ELIF if_condition_block)* (ELSE block)? FI
    ;

if_condition_block
    : condition_block if_statement_block
    ;

if_statement_block
    : THEN block
    ;


// while statement
while_stat
    : WHILE while_condition_block
    ;

while_condition_block
    : condition_block loop_statement_block
    ;


// for statement
for_stat
    : FOR for_condition_block loop_statement_block
    ;

for_condition_block
    : OPAR (assignment | ID SCOL | SCOL)  expr SCOL (expr)? CPAR
    ;


loop_statement_block
    : DO block DONE
    ;

condition_block
    : OPAR expr CPAR
    ;

print
    : PRINTLN expr SCOL                             #printNewLine
    | PRINT expr SCOL                               #printValue
    ;

expr
    : MINUS expr                                                            #minusExpr
    | NOT expr                                                              #notExpr
    | expr op=(MULT | DIV | MOD) expr                                       #multiplicationExpr
    | expr op=(PLUS | MINUS) expr                                           #additiveExpr
    | expr op=(LTEQ | GTEQ | LT | GT) expr                                  #relationalExpr
    | expr op=(EQ | NEQ) expr                                               #equalityExpr
    | expr AND expr                                                         #andExpr
    | expr OR expr                                                          #orExpr
    | compoundAssignment                                                    #compoundExpr
    | atom                                                                  #atomExpr
    | incrDecr                                                              #incrDecrExpr
    ;

atom :
    OPAR expr CPAR                                   #parExpr
    | (INT_LITERAL | FLOAT_LITERAL)                  #numberAtom
    | (TRUE | FALSE)                                 #booleanAtom
    | ID                                             #idAtom
    | STRING_LITERAL                                 #stringAtom
    | NIL                                            #nilAtom
    ;

compoundAssignment
    : ID op=(PLUSASSIGN | MODASSIGN | MINUSASSIGN | DIVASSIGN | MULTASSIGN) expr
    ;

incrDecr
    : INC ID        #preInc
    | ID INC        #postInc
    | DEC ID        #preDec
    | ID DEC        #postDec
    ;


/* Tokens */
OR : '||';
AND : '&&';
NOT : '!';
EQ : '==';
NEQ : '!=';
GT : '>';
LT : '<';
GTEQ : '>=';
LTEQ : '<=';
PLUS : '+';
MINUS : '-';
MULT : '*';
DIV : '/';
MOD : '%';

SCOL : ';';
MODASSIGN : '%=';
PLUSASSIGN : '+=';
MINUSASSIGN : '-=';
DIVASSIGN : '/=';
MULTASSIGN : '*=';
DEC : '--';
INC : '++';
ASSIGN : '=';
OPAR : '(';
CPAR : ')';

TRUE : 'true';
FALSE : 'false';
NIL : 'none';
IF : 'if';
ELIF : 'elif';
ELSE : 'else';
THEN : 'then';
FI : 'fi';
WHILE : 'while';
PRINT : 'print';
PRINTLN : 'println';
FOR : 'for';
DO : 'do';
DONE : 'done';


DATA_TYPE
    : INT
    | FLOAT
    | STRING
    | BOOL
    ;

INT : 'int';
FLOAT : 'float';
STRING : 'string';
BOOL : 'bool';

ID: [a-zA-Z_][a-zA-Z0-9_]*;

INT_LITERAL
        : '0'
        | '-'? [1-9][0-9]*;

FLOAT_LITERAL
        : '0' '.f'?
        | '-'? [1-9][0-9]* '.' [0-9]* 'f'?
        | '-'? [1-9][0-9]* '.f'?;

STRING_LITERAL
        : '"' (~["\r\n] | '""')* '"';


COMMENT_LINE : '//' ~[\r\n]* -> skip;
COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\r\n]+ -> skip;
OTHER: .;