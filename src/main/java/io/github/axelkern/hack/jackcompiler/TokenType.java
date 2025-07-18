package io.github.axelkern.hack.jackcompiler;

public enum TokenType {
    /* @formatter:off */
    // Single-character tokens.
    LEFT_BRACE("{"),
    RIGHT_BRACE("}"),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    LEFT_BRACKET("["),
    RIGHT_BRACKET("]"),
    DOT("."),
    COMMA(","),
    SEMICOLON(";"),
    PLUS("+"),
    MINUS("-"),
    STAR("*"),
    SLASH("/"),
    AND("&"),
    OR("|"),

    // One or two character tokens (not in standard Jack language).
    LESS("<"),
    GREATER(">"),
    EQUAL("="),
    TILDE("~"),

    // Keywords.
    CLASS, CONSTRUCTOR, FUNCTION, METHOD, FIELD, STATIC, VAR, INT, CHAR, BOOLEAN, VOID, TRUE, FALSE, NULL, THIS, LET,
    DO, IF, ELSE, WHILE, RETURN,

    // Literals.
    IDENTIFIER(false), STRING(false), NUMBER(false),

    // Internal Tokens
    NEGSUB(false),
    
    // Comments (handled by Scanner).
    LINE_COMMENT("//"),
    BLOCK_COMMENT("/*"),
    DOC_COMMENT("/**"),
    ANNOTATION("//#"),

    EOF(false);
    /* @formatter:on */

    public final boolean isKeyword;
    public final String lexeme;

    TokenType() {
        this(true);
    }

    TokenType(String symbol) {
        isKeyword = false;
        this.lexeme = symbol;
    }

    TokenType(boolean isKeyword) {
        this.isKeyword = isKeyword;
        if (isKeyword) {
            this.lexeme = this.name().toLowerCase();
        } else {
            this.lexeme = null;
        }
    }

    public enum SubroutineType {
        CONSTRUCTOR, FUNCTION, METHOD
    }

    public enum VarType {
        INT, CHAR, BOOLEAN, CLASS_TYPE
    }

    public enum ReturnType {
        VOID, INT, CHAR, BOOLEAN, CLASS_TYPE
    }
}
