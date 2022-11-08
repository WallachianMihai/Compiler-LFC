grammar BlueJay;

parse
    : block EOF
    ;

block
    : stat*
    ;

stat
    : declaration
    | assignment
    | if_stat
    | while_stat
    | for_stat
    | OTHER {System.err.println("unknown identifier: " + $OTHER.text);}
    ;

declaration
    : DATA_TYPE ID SCOL
    | DATA_TYPE ID ASSIGN expr SCOL
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
    : OPAR ( (expr SCOL | declaration | assignment) | SCOL ) (expr | ID ASSIGN expr) SCOL (expr | ID ASSIGN expr)? CPAR
    ;


loop_statement_block
    : DO block DONE
    ;

condition_block
    : OPAR expr CPAR
    ;

expr
    : MINUS expr                            //Unary minus operator
    | NOT expr                              //Unary not operator
    | expr op=(MULT | DIV | MOD) expr       //Multiplication/division/modulo expressions
    | expr op=(PLUS | MINUS) expr           //additive expressions
    | expr op=(LTEQ | GTEQ | LT | GT) expr  //relational expressions
    | expr op=(EQ | NEQ) expr               //equality expressions
    | expr AND expr                         //and expressions
    | expr OR expr                          //or expressions
    | atom                                  //instantaneous expression
    ;

atom :
    OPAR expr CPAR
    | (INT_LITERAL | FLOAT_LITERAL | STRING_LITERAL) //literal values
    | (TRUE | FALSE)                                 //boolean values
    | ID                                             //identifier
    | NIL                                            //nil
    ;


/* Tokens */
OR : '||' | 'or';
AND : '&&' | 'and';
NOT : '!' | 'not';
EQ : '==' | 'equals' | 'is';
NEQ : '!=' | 'is not';
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