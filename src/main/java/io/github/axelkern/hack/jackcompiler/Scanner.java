package io.github.axelkern.hack.jackcompiler;

import static io.github.axelkern.hack.jackcompiler.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    public record Token(TokenType type, String lexeme, Object literal, String annotation, String doc, int line,
            int column) {
        @Override
        public String toString() {
            return type + " " + lexeme + " " + literal;
        }
    }

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        for (TokenType t : TokenType.values()) {
            if (t.isKeyword) {
                keywords.put(t.lexeme, t);
            }
        }
    }

    private static final Map<Character, List<TokenType>> symbols; // not yet in use
    static {
        symbols = new HashMap<>();
        for (TokenType t : TokenType.values()) {
            if (!t.isKeyword && t.lexeme != null) {
                char firstChar = t.lexeme.charAt(0);
                if (symbols.containsKey(firstChar)) {
                    symbols.get(firstChar).add(t);
                } else {
                    List<TokenType> entry = new ArrayList<>();
                    entry.add(t);
                    symbols.put(firstChar, entry);
                }
            }
        }
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private boolean hasDoc;
    private String doc;
    private boolean hasAnnotation;
    private String annotation;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 0;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        hasDoc = false;
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, null, null, line, column));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
        case '{':
            addToken(LEFT_BRACE);
            break;
        case '}':
            addToken(RIGHT_BRACE);
            break;
        case '(':
            addToken(LEFT_PAREN);
            break;
        case ')':
            addToken(RIGHT_PAREN);
            break;
        case '[':
            addToken(LEFT_BRACKET);
            break;
        case ']':
            addToken(RIGHT_BRACKET);
            break;
        case '.':
            addToken(DOT);
            break;
        case ',':
            addToken(COMMA);
            break;
        case ';':
            addToken(SEMICOLON);
            break;
        case '+':
            addToken(PLUS);
            break;
        case '-':
            addToken(MINUS);
            break;
        case '*':
            addToken(STAR);
            break;
        case '/':
            if (match('/')) { // line comment
                if (match('@')) { // annotation
                    hasAnnotation = true;
                }
                while (peek() != '\n' && !isAtEnd()) {
                    advance();
                }
                if (hasAnnotation) {
                    annotation = source.substring(start + 2, current); // Trim //
                }
            } else if (match('*')) { // block comment
                if (peek() == '*' && peekNext() != '/') { // documentation comment
                    hasDoc = true;
                }
                while ((peek() != ('*') || peekNext() != ('/')) && !isAtEnd()) {
                    if (peek() == '\n') {
                        line++;
                        column = 0;
                    }
                    advance();
                }
                advance(); // consume the * and the /
                advance();
                if (hasDoc) {
                    doc = source.substring(start, current);
                }
            } else {
                addToken(SLASH);
            }
            break;
        case '&':
            addToken(AND);
            break;
        case '|':
            addToken(OR);
            break;
        case '<':
            addToken(LESS);
            break;
        case '>':
            addToken(GREATER);
            break;
        case '=':
            addToken(EQUAL);
            break;
        case '~':
            addToken(TILDE);
            break;
        case ' ':
        case '\r':
        case '\t':
            // Ignore whitespace.
            break;
        case '\n':
            line++;
            column = 0;
            break;
        case '"':
            string();
            break;
        default:
            if (isDigit(c)) {
                number();
            } else if (isAlpha(c)) {
                identifier();
            } else {
                // JackCompiler.error(line, column, "Unexpected character.");
            }
            break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek()))
            advance();
        addToken(NUMBER, Integer.parseInt(source.substring(start, current)));
        // Look for a fractional part.
//        if (peek() == '.' && isDigit(peekNext())) {
//            // Consume the "."
//            advance();
//            while (isDigit(peek()))
//                advance();
//        }
//        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }
        if (isAtEnd()) {
            // JackCompiler.error(line, column, "Unterminated string.");
            return;
        }
        advance(); // closing "
        addToken(STRING, source.substring(start + 1, current - 1)); // Trim the surrounding quotes.
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        column++;
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(
                new Token(type, text, literal, hasAnnotation ? annotation : null, hasDoc ? doc : null, line, column));
        hasDoc = false;
    }
}
