package com.aesirinteractive.angelscript

import com.intellij.lexer.FlexLexer
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

/** In-memory Unicode DFA scanner implemented in Kotlin, preserving all original lexical states. */
class AngelscriptLexer : FlexLexer {
    private var buffer: CharSequence = ""
    private var end = 0
    private var start = 0
    private var marked = 0
    private var lexicalState = YYINITIAL
    private var fstringBraceDepth = 0
    private var fstringParenDepth = 0
    private var fstringBracketDepth = 0
    private var fstringTernaryDepth = 0
    private var fstringIsDouble = false
    private var fstringReturnState = YYINITIAL

    override fun getTokenStart() = start
    override fun getTokenEnd() = marked
    override fun yystate() = lexicalState
    override fun yybegin(newState: Int) { lexicalState = newState }
    override fun reset(buffer: CharSequence, start: Int, end: Int, initialState: Int) {
        this.buffer = buffer
        this.end = end
        this.start = start
        this.marked = start
        if (initialState == YYINITIAL) {
            resetInterpolation()
            fstringIsDouble = false
            fstringReturnState = YYINITIAL
        }
        yybegin(initialState)
    }

    private fun resetInterpolation() {
        fstringBraceDepth = 0
        fstringParenDepth = 0
        fstringBracketDepth = 0
        fstringTernaryDepth = 0
    }

    private fun atInterpolationRoot() =
        fstringBraceDepth == 0 && fstringParenDepth == 0 && fstringBracketDepth == 0

    override fun advance(): IElementType? {
        val tables = AngelscriptLexerTables
        while (true) {
            start = marked
            if (start < end) {
                if (lexicalState == IN_STRING_DQ || lexicalState == IN_STRING_SQ) {
                    return quotedStringToken()
                }
                if (lexicalState == YYINITIAL && buffer[start] == '#') {
                    conditionalDirective()?.let { return it }
                }
            }
            var current = start
            var state = tables.ZZ_LEXSTATE[lexicalState]
            var action = if ((tables.ZZ_ATTRIBUTE[state] and 1) == 1) state else -1
            while (current < end) {
                val codePoint = Character.codePointAt(buffer, current)
                current += Character.charCount(codePoint)
                val low = codePoint and 255
                val charClass = tables.ZZ_CMAP_BLOCKS[if (low == codePoint) low else tables.ZZ_CMAP_TOP[codePoint shr 8] or low]
                val next = tables.ZZ_TRANS[tables.ZZ_ROWMAP[state] + charClass]
                if (next == -1) break
                state = next
                val attributes = tables.ZZ_ATTRIBUTE[state]
                if ((attributes and 1) == 1) {
                    action = state
                    marked = current
                    if ((attributes and 8) == 8) break
                }
            }
            if (start == end) {
                val token = when (lexicalState) {
                    IN_BLOCK_COMMENT -> AngelscriptTypes.COMMENT
                    IN_STRING_DQ, IN_STRING_SQ, IN_RAW_STRING -> AngelscriptTypes.STRING_LITERAL
                    IN_FSTRING_DQ, IN_FSTRING_SQ, IN_FSTRING_FORMAT -> AngelscriptTypes.FSTRING_END
                    else -> return null
                }
                yybegin(YYINITIAL)
                fstringReturnState = YYINITIAL
                return token
            }
            when (if (action < 0) action else tables.ZZ_ACTION[action]) {
                1 -> { return TokenType.BAD_CHARACTER; }
                2 -> { return TokenType.WHITE_SPACE; }
                3 -> { return AngelscriptTypes.EXCL; }
                4 -> { yybegin(IN_STRING_DQ); return AngelscriptTypes.STRING_LITERAL; }
                5 -> { return AngelscriptTypes.SHA; }
                6 -> { return AngelscriptTypes.REM; }
                7 -> { return AngelscriptTypes.AND; }
                8 -> { yybegin(IN_STRING_SQ); return AngelscriptTypes.STRING_LITERAL; }
                9 -> { if (lexicalState == IN_FSTRING_INTERP) fstringParenDepth++; return AngelscriptTypes.LPAREN; }
                10 -> { if (lexicalState == IN_FSTRING_INTERP && fstringParenDepth > 0) fstringParenDepth--; return AngelscriptTypes.RPAREN; }
                11 -> { return AngelscriptTypes.MUL; }
                12 -> { return AngelscriptTypes.PLUS; }
                13 -> { return AngelscriptTypes.COMMA; }
                14 -> { return AngelscriptTypes.MINUS; }
                15 -> { return AngelscriptTypes.DOT; }
                16 -> { return AngelscriptTypes.DIV; }
                17 -> { return AngelscriptTypes.NUMBER_LITERAL; }
                18 -> { return AngelscriptTypes.COLON; }
                19 -> { return AngelscriptTypes.SEMICOLON; }
                20 -> { return AngelscriptTypes.LT; }
                21 -> { return AngelscriptTypes.EQ; }
                22 -> { return AngelscriptTypes.GT; }
                23 -> { if (lexicalState == IN_FSTRING_INTERP && atInterpolationRoot()) fstringTernaryDepth++; return AngelscriptTypes.QUEST; }
                24 -> { return AngelscriptTypes.AT; }
                25 -> {
                    return if (marked - start == 5 && buffer.regionMatches(start, "local", 0, 5))
                        AngelscriptTypes.LOCAL_KW else AngelscriptTypes.IDENTIFIER
                }
                26 -> { if (lexicalState == IN_FSTRING_INTERP) fstringBracketDepth++; return AngelscriptTypes.LBRACK; }
                27 -> { if (lexicalState == IN_FSTRING_INTERP && fstringBracketDepth > 0) fstringBracketDepth--; return AngelscriptTypes.RBRACK; }
                28 -> { return AngelscriptTypes.XOR; }
                29 -> { return AngelscriptTypes.LBRACE; }
                30 -> { return AngelscriptTypes.OR; }
                31 -> { return AngelscriptTypes.RBRACE; }
                32 -> { return AngelscriptTypes.TILDE; }
                33 -> { return AngelscriptTypes.COMMENT; }
                34 -> { return AngelscriptTypes.STRING_LITERAL; }
                35 -> { val ret = fstringReturnState; fstringReturnState=YYINITIAL; yybegin(ret); return AngelscriptTypes.STRING_LITERAL; }
                36 -> { return AngelscriptTypes.FSTRING_TEXT; }
                37 -> { yybegin(YYINITIAL); return AngelscriptTypes.FSTRING_END; }
                38 -> { resetInterpolation(); yybegin(IN_FSTRING_INTERP); return AngelscriptTypes.FSTRING_LBRACE; }
                39 -> { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_STRING_DQ); return AngelscriptTypes.STRING_LITERAL; }
                40 -> { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_STRING_SQ); return AngelscriptTypes.STRING_LITERAL; }
                41 -> {
                    // Colons in calls, subscripts and conditional expressions belong to AS.
                    // Only a colon after the complete interpolation starts a format suffix.
                    if (!atInterpolationRoot()) return AngelscriptTypes.COLON
                    if (fstringTernaryDepth > 0) {
                        fstringTernaryDepth--
                        return AngelscriptTypes.COLON
                    }
                    yybegin(IN_FSTRING_FORMAT)
                    return AngelscriptTypes.FSTRING_COLON
                }
                42 -> { fstringBraceDepth++; return AngelscriptTypes.LBRACE; }
                43 -> { if (fstringBraceDepth > 0) { fstringBraceDepth--; return AngelscriptTypes.RBRACE; }
	          yybegin(if (fstringIsDouble) IN_FSTRING_DQ else IN_FSTRING_SQ);
	          return AngelscriptTypes.FSTRING_RBRACE; }
                44 -> { return AngelscriptTypes.FSTRING_FORMAT_TEXT; }
                45 -> { yybegin(if (fstringIsDouble) IN_FSTRING_DQ else IN_FSTRING_SQ);
	            return AngelscriptTypes.FSTRING_RBRACE; }
                46 -> { return AngelscriptTypes.EXCLEQ; }
                47 -> { return AngelscriptTypes.REMEQ; }
                48 -> { return AngelscriptTypes.ANDAND; }
                49 -> { return AngelscriptTypes.ANDEQ; }
                50 -> { return AngelscriptTypes.POWER; }
                51 -> { return AngelscriptTypes.MULEQ; }
                52 -> { return AngelscriptTypes.PLUSPLUS; }
                53 -> { return AngelscriptTypes.PLUSEQ; }
                54 -> { return AngelscriptTypes.MINUSMINUS; }
                55 -> { return AngelscriptTypes.MINUSEQ; }
                56 -> { fstringReturnState=YYINITIAL; yybegin(IN_BLOCK_COMMENT); return AngelscriptTypes.COMMENT; }
                57 -> { return AngelscriptTypes.DIVEQ; }
                58 -> { return AngelscriptTypes.COLONCOLON; }
                59 -> { return AngelscriptTypes.LTLT; }
                60 -> { return AngelscriptTypes.LTEQ; }
                61 -> { return AngelscriptTypes.EQEQ; }
                62 -> { return AngelscriptTypes.GTEQ; }
                63 -> { return AngelscriptTypes.XOREQ; }
                64 -> { return AngelscriptTypes.DO_KW; }
                65 -> { fstringIsDouble=true;  yybegin(IN_FSTRING_DQ); return AngelscriptTypes.FSTRING_START; }
                66 -> { fstringIsDouble=false; yybegin(IN_FSTRING_SQ); return AngelscriptTypes.FSTRING_START; }
                67 -> { return AngelscriptTypes.IF; }
                68 -> { return AngelscriptTypes.IN; }
                69 -> { return AngelscriptTypes.IS_KW; }
                70 -> { return AngelscriptTypes.OF_KW; }
                71 -> { return AngelscriptTypes.OR_KW; }
                72 -> { return AngelscriptTypes.OREQ; }
                73 -> { return AngelscriptTypes.OROR; }
                74 -> { yybegin(fstringReturnState); fstringReturnState=YYINITIAL; return AngelscriptTypes.COMMENT; }
                75 -> { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_BLOCK_COMMENT); return AngelscriptTypes.COMMENT; }
                76 -> { yybegin(IN_RAW_STRING); return AngelscriptTypes.STRING_LITERAL; }
                77 -> { return AngelscriptTypes.POWEREQ; }
                78 -> { return AngelscriptTypes.LSHIFTEQ; }
                79 -> { return AngelscriptTypes.RSHIFTEQ; }
                80 -> { return AngelscriptTypes.AND_KW; }
                81 -> { return AngelscriptTypes.FOR; }
                82 -> { return AngelscriptTypes.INT_KW; }
                83 -> { return AngelscriptTypes.NOT_KW; }
                84 -> { return AngelscriptTypes.OUT; }
                85 -> { return AngelscriptTypes.XOR_KW; }
                86 -> { fstringReturnState=IN_FSTRING_INTERP; yybegin(IN_RAW_STRING); return AngelscriptTypes.STRING_LITERAL; }
                87 -> { return AngelscriptTypes.PP_IF; }
                88 -> { return AngelscriptTypes.RRSHIFTEQ; }
                89 -> { return AngelscriptTypes.AUTO; }
                90 -> { return AngelscriptTypes.NULLPTR_KW; }
                91 -> { return AngelscriptTypes.BOOL_LITERAL; }
                92 -> { return AngelscriptTypes.AUTO_KW; }
                93 -> { return AngelscriptTypes.BOOL_KW; }
                94 -> { return AngelscriptTypes.CASE_KW; }
                95 -> { return AngelscriptTypes.ELSE; }
                96 -> { return AngelscriptTypes.ENUM; }
                97 -> { return AngelscriptTypes.INT8_KW; }
                98 -> { return AngelscriptTypes.UINT_KW; }
                99 -> { return AngelscriptTypes.VOID_KW; }
                100 -> { return AngelscriptTypes.PP_ELSE; }
                101 -> { return AngelscriptTypes.UENUM_KW; }
                102 -> { return AngelscriptTypes.ASSET_KW; }
                103 -> { return AngelscriptTypes.BREAK; }
                104 -> { return AngelscriptTypes.CLASS; }
                105 -> { return AngelscriptTypes.CONST; }
                106 -> { return AngelscriptTypes.EVENT_KW; }
                107 -> { return AngelscriptTypes.FINAL_KW; }
                108 -> { return AngelscriptTypes.FLOAT_KW; }
                109 -> { return AngelscriptTypes.INOUT_KW; }
                110 -> { return AngelscriptTypes.INT16_KW; }
                111 -> { return AngelscriptTypes.INT32_KW; }
                112 -> { return AngelscriptTypes.INT64_KW; }
                113 -> { return AngelscriptTypes.MIXIN_KW; }
                114 -> { return AngelscriptTypes.SUPER; }
                115 -> { return AngelscriptTypes.UINT8_KW; }
                116 -> { return AngelscriptTypes.WHILE; }
                117 -> { return AngelscriptTypes.PP_ELIF; }
                118 -> { return AngelscriptTypes.PP_ENDIF; }
                119 -> { return AngelscriptTypes.UCLASS_KW; }
                120 -> { return AngelscriptTypes.DOUBLE_KW; }
                121 -> { return AngelscriptTypes.PUBLIC; }
                122 -> { return AngelscriptTypes.RETURN; }
                123 -> { return AngelscriptTypes.SHARED_KW; }
                124 -> { return AngelscriptTypes.STATIC; }
                125 -> { return AngelscriptTypes.STRUCT; }
                126 -> { return AngelscriptTypes.SWITCH_KW; }
                127 -> { return AngelscriptTypes.UINT16_KW; }
                128 -> { return AngelscriptTypes.UINT32_KW; }
                129 -> { return AngelscriptTypes.UINT64_KW; }
                130 -> { return AngelscriptTypes.USTRUCT_KW; }
                131 -> { return AngelscriptTypes.DEFAULT_KW; }
                132 -> { return AngelscriptTypes.PRIVATE_KW; }
                133 -> { return AngelscriptTypes.TYPEDEF_KW; }
                134 -> { return AngelscriptTypes.PP_DEFINE; }
                135 -> { return AngelscriptTypes.CONTINUE; }
                136 -> { return AngelscriptTypes.DELEGATE_KW; }
                137 -> { return AngelscriptTypes.EXPLICIT_KW; }
                138 -> { return AngelscriptTypes.EXTERNAL_KW; }
                139 -> { return AngelscriptTypes.OVERRIDE_KW; }
                140 -> { return AngelscriptTypes.PROPERTY_KW; }
                141 -> { return AngelscriptTypes.UFUNCTION_KW; }
                142 -> { return AngelscriptTypes.UPROPERTY_KW; }
                143 -> { return AngelscriptTypes.INTERFACE_KW; }
                144 -> { return AngelscriptTypes.NAMESPACE_KW; }
                145 -> { return AngelscriptTypes.PROTECTED_KW; }
                146 -> { return AngelscriptTypes.UINTERFACE_KW; }
                else -> error("Unable to scan token at $start")
            }
        }
    }

    private fun quotedStringToken(): IElementType {
        val quote = if (lexicalState == IN_STRING_DQ) '"' else '\''
        if (buffer[marked] == quote) {
            marked++
            yybegin(fstringReturnState)
            fstringReturnState = YYINITIAL
        } else {
            // Consume escape pairs together: an odd run of backslashes escapes the
            // quote, while an even run leaves it as the closing delimiter.
            while (marked < end && buffer[marked] != quote) {
                marked += if (buffer[marked] == '\\' && marked + 1 < end) 2 else 1
            }
        }
        return AngelscriptTypes.STRING_LITERAL
    }

    private fun conditionalDirective(): IElementType? {
        // The retained DFA handles #if, but lacks the engine's #ifdef/#ifndef forms.
        val directive = when {
            buffer.regionMatches(start, "#ifdef", 0, 6) -> "#ifdef"
            buffer.regionMatches(start, "#ifndef", 0, 7) -> "#ifndef"
            else -> return null
        }
        val after = start + directive.length
        if (after > end || (after < end && !buffer[after].isWhitespace())) return null
        marked = after
        while (marked < end && buffer[marked] != '\r' && buffer[marked] != '\n') marked++
        return AngelscriptTypes.PP_IF
    }

    companion object {
        const val YYINITIAL = 0
        const val IN_BLOCK_COMMENT = 2
        const val IN_STRING_DQ = 4
        const val IN_STRING_SQ = 6
        const val IN_RAW_STRING = 8
        const val IN_FSTRING_DQ = 10
        const val IN_FSTRING_SQ = 12
        const val IN_FSTRING_INTERP = 14
        const val IN_FSTRING_FORMAT = 16
    }
}
