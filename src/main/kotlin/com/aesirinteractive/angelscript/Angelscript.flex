package com.aesirinteractive.angelscript;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.aesirinteractive.angelscript.AngelscriptTypes;
import com.intellij.psi.TokenType;

%%

%{
	public AngelscriptLexer() {
		this((java.io.Reader)null);
	}

	private int fstringBraceDepth = 0;
	private boolean fstringIsDouble = false;
	// State to return to after a nested string/comment finishes inside an f-string interpolation
	// (0 == YYINITIAL; the constant isn't available at field-init time)
	private int fstringReturnState = 0;
%}

%class AngelscriptLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{
	return;
%eof}

%s IN_BLOCK_COMMENT
%s IN_STRING_DQ
%s IN_STRING_SQ
%s IN_RAW_STRING
%s IN_FSTRING_DQ
%s IN_FSTRING_SQ
%s IN_FSTRING_INTERP
%s IN_FSTRING_FORMAT

///////////////////////////////////////////////////////////////////////////////////////////////////
// Whitespace
///////////////////////////////////////////////////////////////////////////////////////////////////

EOL_WS           = \n | \r | \r\n
LINE_WS          = [\ \t\f]
WHITE_SPACE      = ({LINE_WS}|{EOL_WS})+

///////////////////////////////////////////////////////////////////////////////////////////////////
// Identifiers
///////////////////////////////////////////////////////////////////////////////////////////////////

IDENTIFIER       = [_\p{XID_Start}][_\p{XID_Continue}]*

///////////////////////////////////////////////////////////////////////////////////////////////////
// Numbers
///////////////////////////////////////////////////////////////////////////////////////////////////

DEC_DIGIT        = [0-9]
HEX_DIGIT        = [0-9a-fA-F]
BIN_DIGIT        = [01]
EXPONENT         = [eE][\+\-]?{DEC_DIGIT}+
DIGIT_SEP        = "'"

DEC_INTEGER      = {DEC_DIGIT}+({DIGIT_SEP}{DEC_DIGIT}+)*
HEX_INTEGER      = 0[xX]{HEX_DIGIT}+({DIGIT_SEP}{HEX_DIGIT}+)*
BIN_INTEGER      = 0[bB]{BIN_DIGIT}+({DIGIT_SEP}{BIN_DIGIT}+)*

NUM_SUFFIX       = [fFdDuUlL]

FLOAT_NUMBER     = (
									 {DEC_INTEGER}\.{DEC_DIGIT}*({EXPONENT})?{NUM_SUFFIX}?
								 | \.{DEC_DIGIT}+({EXPONENT})?{NUM_SUFFIX}?
								 | {DEC_INTEGER}{EXPONENT}{NUM_SUFFIX}?
								 )

///////////////////////////////////////////////////////////////////////////////////////////////////
// Strings and comments
///////////////////////////////////////////////////////////////////////////////////////////////////

ESCAPE_SEQUENCE  = \\([^xuU] | [0-9]{2,3} | x{HEX_DIGIT}{2} | u{HEX_DIGIT}{4} | U{HEX_DIGIT}{8})
RAW_PREFIX       = [A-Za-z_][A-Za-z0-9_]*

%%

///////////////////////////////////////////////////////////////////////////////////////////////////
// Rules shared between YYINITIAL and IN_FSTRING_INTERP
// (everything whose action is identical in both states)
///////////////////////////////////////////////////////////////////////////////////////////////////

<YYINITIAL, IN_FSTRING_INTERP> {
	{WHITE_SPACE}                    { return TokenType.WHITE_SPACE; }

	"//" .*                          { return AngelscriptTypes.COMMENT; }

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Preprocessor directives — consume the entire line
	///////////////////////////////////////////////////////////////////////////////////////////////////

	"#"{LINE_WS}*"if"{LINE_WS}+[^\r\n]*    { return AngelscriptTypes.PP_IF; }
	"#"{LINE_WS}*"elif"{LINE_WS}+[^\r\n]*  { return AngelscriptTypes.PP_ELIF; }
	"#"{LINE_WS}*"else"{LINE_WS}*[^\r\n]*  { return AngelscriptTypes.PP_ELSE; }
	"#"{LINE_WS}*"endif"{LINE_WS}*[^\r\n]* { return AngelscriptTypes.PP_ENDIF; }
	"#"{LINE_WS}*"define"{LINE_WS}+[^\r\n]* { return AngelscriptTypes.PP_DEFINE; }

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Literals and identifiers
	///////////////////////////////////////////////////////////////////////////////////////////////////

	{FLOAT_NUMBER}                   { return AngelscriptTypes.NUMBER_LITERAL; }
	{HEX_INTEGER}{NUM_SUFFIX}?|{BIN_INTEGER}{NUM_SUFFIX}?|{DEC_INTEGER}{NUM_SUFFIX}?
	                                 { return AngelscriptTypes.NUMBER_LITERAL; }

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Punctuation and operators
	// (excludes {, }, : which differ between states)
	///////////////////////////////////////////////////////////////////////////////////////////////////

	"["                             { return AngelscriptTypes.LBRACK; }
	"]"                             { return AngelscriptTypes.RBRACK; }
	"("                             { return AngelscriptTypes.LPAREN; }
	")"                             { return AngelscriptTypes.RPAREN; }
	"::"                            { return AngelscriptTypes.COLONCOLON; }
	";"                             { return AngelscriptTypes.SEMICOLON; }
	","                             { return AngelscriptTypes.COMMA; }
	"."                             { return AngelscriptTypes.DOT; }
	"!="                            { return AngelscriptTypes.EXCLEQ; }
	"=="                            { return AngelscriptTypes.EQEQ; }
	"!"                             { return AngelscriptTypes.EXCL; }
	"+="                            { return AngelscriptTypes.PLUSEQ; }
	"++"                            { return AngelscriptTypes.PLUSPLUS; }
	"+"                             { return AngelscriptTypes.PLUS; }
	"--"                            { return AngelscriptTypes.MINUSMINUS; }
	"-="                            { return AngelscriptTypes.MINUSEQ; }
	"-"                             { return AngelscriptTypes.MINUS; }
	"#"                             { return AngelscriptTypes.SHA; }
	"@"                             { return AngelscriptTypes.AT; }
	"~"                             { return AngelscriptTypes.TILDE; }
	"?"                             { return AngelscriptTypes.QUEST; }
	"|="                            { return AngelscriptTypes.OREQ; }
	"||"                            { return AngelscriptTypes.OROR; }
	"&="                            { return AngelscriptTypes.ANDEQ; }
	"&&"                            { return AngelscriptTypes.ANDAND; }
	"&"                             { return AngelscriptTypes.AND; }
	"|"                             { return AngelscriptTypes.OR; }
	// Compound operators involving < and > must come before single-char versions
	">>>="                          { return AngelscriptTypes.RRSHIFTEQ; }
	">>="                           { return AngelscriptTypes.RSHIFTEQ; }
	">="                            { return AngelscriptTypes.GTEQ; }
	"<<="                           { return AngelscriptTypes.LSHIFTEQ; }
	"<<"                            { return AngelscriptTypes.LTLT; }
	"<="                            { return AngelscriptTypes.LTEQ; }
	"<"                             { return AngelscriptTypes.LT; }
	">"                             { return AngelscriptTypes.GT; }
	"^="                            { return AngelscriptTypes.XOREQ; }
	"^"                             { return AngelscriptTypes.XOR; }
	"**="                           { return AngelscriptTypes.POWEREQ; }
	"**"                            { return AngelscriptTypes.POWER; }
	"*="                            { return AngelscriptTypes.MULEQ; }
	"*"                             { return AngelscriptTypes.MUL; }
	"/="                            { return AngelscriptTypes.DIVEQ; }
	"/"                             { return AngelscriptTypes.DIV; }
	"%="                            { return AngelscriptTypes.REMEQ; }
	"%"                             { return AngelscriptTypes.REM; }
	"="                             { return AngelscriptTypes.EQ; }

	"true"|"false"|"TRUE"|"FALSE"   { return AngelscriptTypes.BOOL_LITERAL; }
	"nullptr"|"null"|"NULL"         { return AngelscriptTypes.NULLPTR_KW; }
	"not"                           { return AngelscriptTypes.NOT_KW; }
	"and"                           { return AngelscriptTypes.AND_KW; }
	"or"                            { return AngelscriptTypes.OR_KW; }
	"xor"                           { return AngelscriptTypes.XOR_KW; }
	"is"                            { return AngelscriptTypes.IS_KW; }
	"void"                          { return AngelscriptTypes.VOID_KW; }
	"int"                           { return AngelscriptTypes.INT_KW; }
	"int8"                          { return AngelscriptTypes.INT8_KW; }
	"int16"                         { return AngelscriptTypes.INT16_KW; }
	"int32"                         { return AngelscriptTypes.INT32_KW; }
	"int64"                         { return AngelscriptTypes.INT64_KW; }
	"uint"                          { return AngelscriptTypes.UINT_KW; }
	"uint8"                         { return AngelscriptTypes.UINT8_KW; }
	"uint16"                        { return AngelscriptTypes.UINT16_KW; }
	"uint32"                        { return AngelscriptTypes.UINT32_KW; }
	"uint64"                        { return AngelscriptTypes.UINT64_KW; }
	"float"                         { return AngelscriptTypes.FLOAT_KW; }
	"double"                        { return AngelscriptTypes.DOUBLE_KW; }
	"bool"                          { return AngelscriptTypes.BOOL_KW; }
	"auto"                          { return AngelscriptTypes.AUTO_KW; }
	"switch"                        { return AngelscriptTypes.SWITCH_KW; }
	"case"                          { return AngelscriptTypes.CASE_KW; }
	"default"                       { return AngelscriptTypes.DEFAULT_KW; }
	"do"                            { return AngelscriptTypes.DO_KW; }
	"typedef"                       { return AngelscriptTypes.TYPEDEF_KW; }
	"event"                         { return AngelscriptTypes.EVENT_KW; }
	"delegate"                      { return AngelscriptTypes.DELEGATE_KW; }
	"namespace"                     { return AngelscriptTypes.NAMESPACE_KW; }
	"mixin"                         { return AngelscriptTypes.MIXIN_KW; }
	"asset"                         { return AngelscriptTypes.ASSET_KW; }
	"of"                            { return AngelscriptTypes.OF_KW; }
	"shared"                        { return AngelscriptTypes.SHARED_KW; }
	"external"                      { return AngelscriptTypes.EXTERNAL_KW; }
	"private"                       { return AngelscriptTypes.PRIVATE_KW; }
	"protected"                     { return AngelscriptTypes.PROTECTED_KW; }
	"override"                      { return AngelscriptTypes.OVERRIDE_KW; }
	"final"                         { return AngelscriptTypes.FINAL_KW; }
	"explicit"                      { return AngelscriptTypes.EXPLICIT_KW; }
	"property"                      { return AngelscriptTypes.PROPERTY_KW; }
	"inout"                         { return AngelscriptTypes.INOUT_KW; }
	"interface"                     { return AngelscriptTypes.INTERFACE_KW; }
	"UFUNCTION"                     { return AngelscriptTypes.UFUNCTION_KW; }
	"UPROPERTY"                     { return AngelscriptTypes.UPROPERTY_KW; }
	"UCLASS"                        { return AngelscriptTypes.UCLASS_KW; }
	"USTRUCT"                       { return AngelscriptTypes.USTRUCT_KW; }
	"UENUM"                         { return AngelscriptTypes.UENUM_KW; }
	"UINTERFACE"                    { return AngelscriptTypes.UINTERFACE_KW; }
	"break"                         { return AngelscriptTypes.BREAK; }
	"const"                         { return AngelscriptTypes.CONST; }
	"continue"                      { return AngelscriptTypes.CONTINUE; }
	"else"                          { return AngelscriptTypes.ELSE; }
	"enum"                          { return AngelscriptTypes.ENUM; }
	"for"                           { return AngelscriptTypes.FOR; }
	"if"                            { return AngelscriptTypes.IF; }
	"out"                           { return AngelscriptTypes.OUT; }
	"in"                            { return AngelscriptTypes.IN; }
	"AUTO"                          { return AngelscriptTypes.AUTO; }
	"while"                         { return AngelscriptTypes.WHILE; }
	"public"                        { return AngelscriptTypes.PUBLIC; }
	"return"                        { return AngelscriptTypes.RETURN; }
	"static"                        { return AngelscriptTypes.STATIC; }
	"struct"                        { return AngelscriptTypes.STRUCT; }
	"class"                         { return AngelscriptTypes.CLASS; }
	"super"                         { return AngelscriptTypes.SUPER; }

	{IDENTIFIER}                    { return AngelscriptTypes.IDENTIFIER; }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// YYINITIAL-only rules
///////////////////////////////////////////////////////////////////////////////////////////////////

<YYINITIAL> {
	"/*"                             { fstringReturnState=YYINITIAL; yybegin(IN_BLOCK_COMMENT); return AngelscriptTypes.COMMENT; }

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Strings
	///////////////////////////////////////////////////////////////////////////////////////////////////

	"\"\"\""                        { yybegin(IN_RAW_STRING); return AngelscriptTypes.STRING_LITERAL; }
	{RAW_PREFIX}"\"\"\""             { yybegin(IN_RAW_STRING); return AngelscriptTypes.STRING_LITERAL; }

	// F-strings: must appear before generic {RAW_PREFIX}"\"" rule
	"f\""                           { fstringIsDouble=true;  yybegin(IN_FSTRING_DQ); return AngelscriptTypes.FSTRING_START; }
	"f'"                            { fstringIsDouble=false; yybegin(IN_FSTRING_SQ); return AngelscriptTypes.FSTRING_START; }

	{RAW_PREFIX}"\""                 { yybegin(IN_STRING_DQ); return AngelscriptTypes.STRING_LITERAL; }
	{RAW_PREFIX}"'"                  { yybegin(IN_STRING_SQ); return AngelscriptTypes.STRING_LITERAL; }
	"\""                            { yybegin(IN_STRING_DQ); return AngelscriptTypes.STRING_LITERAL; }
	"'"                              { yybegin(IN_STRING_SQ); return AngelscriptTypes.STRING_LITERAL; }

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Braces and colon (normal behaviour outside f-strings)
	///////////////////////////////////////////////////////////////////////////////////////////////////

	"{"                             { return AngelscriptTypes.LBRACE; }
	"}"                             { return AngelscriptTypes.RBRACE; }
	":"                             { return AngelscriptTypes.COLON; }
	[^]                             { return TokenType.BAD_CHARACTER; }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Block comment — returns to fstringReturnState (YYINITIAL or IN_FSTRING_INTERP)
///////////////////////////////////////////////////////////////////////////////////////////////////

<IN_BLOCK_COMMENT> {
	"*/"                             { yybegin(fstringReturnState); fstringReturnState=YYINITIAL; return AngelscriptTypes.COMMENT; }
	[^]                              { return AngelscriptTypes.COMMENT; }
	<<EOF>>                          { yybegin(YYINITIAL); fstringReturnState=YYINITIAL; return AngelscriptTypes.COMMENT; }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Normal string states — return to fstringReturnState when done
///////////////////////////////////////////////////////////////////////////////////////////////////

<IN_STRING_DQ> {
	{ESCAPE_SEQUENCE}                 { return AngelscriptTypes.STRING_LITERAL; }
	"\\\r?\n"                       { return AngelscriptTypes.STRING_LITERAL; }
	"\""                            { int ret=fstringReturnState; fstringReturnState=YYINITIAL; yybegin(ret); return AngelscriptTypes.STRING_LITERAL; }
	[^\"\r\n]+                     { return AngelscriptTypes.STRING_LITERAL; }
	[^]                               { int ret=fstringReturnState; fstringReturnState=YYINITIAL; yybegin(ret); return AngelscriptTypes.STRING_LITERAL; }
	<<EOF>>                          { yybegin(YYINITIAL); fstringReturnState=YYINITIAL; return AngelscriptTypes.STRING_LITERAL; }
}

<IN_STRING_SQ> {
	{ESCAPE_SEQUENCE}                 { return AngelscriptTypes.STRING_LITERAL; }
	"\\\r?\n"                       { return AngelscriptTypes.STRING_LITERAL; }
	"'"                              { int ret=fstringReturnState; fstringReturnState=YYINITIAL; yybegin(ret); return AngelscriptTypes.STRING_LITERAL; }
	[^\\'\r\n]+                     { return AngelscriptTypes.STRING_LITERAL; }
	[^]                               { int ret=fstringReturnState; fstringReturnState=YYINITIAL; yybegin(ret); return AngelscriptTypes.STRING_LITERAL; }
	<<EOF>>                          { yybegin(YYINITIAL); fstringReturnState=YYINITIAL; return AngelscriptTypes.STRING_LITERAL; }
}

<IN_RAW_STRING> {
	"\"\"\""                        { int ret=fstringReturnState; fstringReturnState=YYINITIAL; yybegin(ret); return AngelscriptTypes.STRING_LITERAL; }
	[^]                               { return AngelscriptTypes.STRING_LITERAL; }
	<<EOF>>                          { yybegin(YYINITIAL); fstringReturnState=YYINITIAL; return AngelscriptTypes.STRING_LITERAL; }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// F-string states
///////////////////////////////////////////////////////////////////////////////////////////////////

<IN_FSTRING_DQ> {
	"{{"                            { return AngelscriptTypes.FSTRING_TEXT; }
	"{"                             { fstringBraceDepth=0; yybegin(IN_FSTRING_INTERP); return AngelscriptTypes.FSTRING_LBRACE; }
	"}"                             { return AngelscriptTypes.FSTRING_TEXT; }
	"\""                            { yybegin(YYINITIAL); return AngelscriptTypes.FSTRING_END; }
	{ESCAPE_SEQUENCE}               { return AngelscriptTypes.FSTRING_TEXT; }
	[^\"\{\}\\\r\n]+                { return AngelscriptTypes.FSTRING_TEXT; }
	[^]                             { yybegin(YYINITIAL); return AngelscriptTypes.FSTRING_END; }
	<<EOF>>                         { yybegin(YYINITIAL); return AngelscriptTypes.FSTRING_END; }
}

<IN_FSTRING_SQ> {
	"{{"                            { return AngelscriptTypes.FSTRING_TEXT; }
	"{"                             { fstringBraceDepth=0; yybegin(IN_FSTRING_INTERP); return AngelscriptTypes.FSTRING_LBRACE; }
	"}"                             { return AngelscriptTypes.FSTRING_TEXT; }
	"'"                             { yybegin(YYINITIAL); return AngelscriptTypes.FSTRING_END; }
	{ESCAPE_SEQUENCE}               { return AngelscriptTypes.FSTRING_TEXT; }
	[^'\{\}\\\r\n]+                 { return AngelscriptTypes.FSTRING_TEXT; }
	[^]                             { yybegin(YYINITIAL); return AngelscriptTypes.FSTRING_END; }
	<<EOF>>                         { yybegin(YYINITIAL); return AngelscriptTypes.FSTRING_END; }
}

// IN_FSTRING_INTERP: lex expressions inside { }.
// Most token rules are shared with YYINITIAL via the <YYINITIAL, IN_FSTRING_INTERP> group above.
// Only the rules that differ between states are listed here.
<IN_FSTRING_INTERP> {
	"/*"    { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_BLOCK_COMMENT); return AngelscriptTypes.COMMENT; }

	// Strings inside interpolations: save return state so we come back here after the string ends
	"\"\"\""                        { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_RAW_STRING); return AngelscriptTypes.STRING_LITERAL; }
	{RAW_PREFIX}"\"\"\""             { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_RAW_STRING); return AngelscriptTypes.STRING_LITERAL; }
	{RAW_PREFIX}"\""                 { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_STRING_DQ); return AngelscriptTypes.STRING_LITERAL; }
	{RAW_PREFIX}"'"                  { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_STRING_SQ); return AngelscriptTypes.STRING_LITERAL; }
	"\""                            { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_STRING_DQ); return AngelscriptTypes.STRING_LITERAL; }
	"'"                              { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_STRING_SQ); return AngelscriptTypes.STRING_LITERAL; }

	// Brace tracking and format-spec colon
	"{"   { fstringBraceDepth++; return AngelscriptTypes.LBRACE; }
	"}"   {
	          if (fstringBraceDepth > 0) { fstringBraceDepth--; return AngelscriptTypes.RBRACE; }
	          yybegin(fstringIsDouble ? IN_FSTRING_DQ : IN_FSTRING_SQ);
	          return AngelscriptTypes.FSTRING_RBRACE;
	      }
	":"   {
	          if (fstringBraceDepth > 0) return AngelscriptTypes.COLON;
	          yybegin(IN_FSTRING_FORMAT);
	          return AngelscriptTypes.FSTRING_COLON;
	      }
	[^]   { return TokenType.BAD_CHARACTER; }
}

// Format specifier: raw text after the : until the closing }
<IN_FSTRING_FORMAT> {
	"}"     {
	            yybegin(fstringIsDouble ? IN_FSTRING_DQ : IN_FSTRING_SQ);
	            return AngelscriptTypes.FSTRING_RBRACE;
	        }
	[^\}]+  { return AngelscriptTypes.FSTRING_FORMAT_TEXT; }
	<<EOF>> { yybegin(YYINITIAL); return AngelscriptTypes.FSTRING_END; }
}

[^]                                                         { return TokenType.BAD_CHARACTER; }
