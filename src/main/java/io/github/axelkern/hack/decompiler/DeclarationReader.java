package io.github.axelkern.hack.decompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import io.github.axelkern.hack.jackcompiler.Scanner;
import io.github.axelkern.hack.jackcompiler.TokenType;
import io.github.axelkern.hack.jackcompiler.Scanner.Token;

class DeclarationReader {
    private String className;
    private String functionName;
    private String functionType;
    private SymbolTable symbols;
    private List<Token> tokens;
    private int currentToken;
    private int statics;
    private int fields;
    private int vars;
    private String name;
    private String type;

    public static void readDeclarations(SymbolTable symbols) {
        new DeclarationReader(symbols);
    }

    private DeclarationReader(SymbolTable symbols) {
        String fileName = io.github.axelkern.hack.util.Util.getJarLocation() + File.separator + "decompiler.def";
        String declarations;
        if (Files.exists(Paths.get(fileName))) {
            System.out.print("Parsing external declarations... ");
            declarations = io.github.axelkern.hack.util.Util.readFileAsString(Paths.get(fileName));
        } else {
            try {
                declarations = new String(getClass().getResourceAsStream("decompiler.def").readAllBytes());
                System.out.print("Parsing internal declarations... ");
            } catch (IOException e) {
                declarations = null;
            }
        }
        if (declarations != null) {
            this.symbols = symbols;
            Scanner scanner = new Scanner(declarations);
            tokens = scanner.scanTokens();
            currentToken = 0;
            parseTokens();
            System.out.println("found " + symbols.size() + " entries.");
        }
    }

    private Token peek() {
        return tokens.get(currentToken);
    }

    private Token getLast() {
        return tokens.get(currentToken - 1);
    }

    private Token consume() {
        if (currentToken < tokens.size()) {
            currentToken++;
            return tokens.get(currentToken - 1);
        } else {
            return null;
        }
    }

    private boolean parseType() {
        Token token = consume();
        if (token.type() == TokenType.IDENTIFIER) {
            type = token.lexeme();
            return true;
        } else if (token.type() == TokenType.BOOLEAN || token.type() == TokenType.CHAR || token.type() == TokenType.INT
                || token.type() == TokenType.VOID) {
            type = token.type().name().toLowerCase();
            return true;
        } else {
            return false;
        }
    }

    private boolean parseIdentifier() {
        Token token = consume();
        if (token.type() == TokenType.IDENTIFIER) {
            name = token.lexeme();
            return true;
        } else {
            return false;
        }
    }

    private boolean parseTypedName() {
        if (!parseType()) {
            return false;
        }
        return (parseIdentifier());
    }

    void parseTokens() {
        while (currentToken < tokens.size()) {
            Token token = consume();
            switch (token.type()) {
            case CLASS:
                parseClass();
                break;
            case STATIC:
            case FIELD:
                parseClassVarDec();
                break;
            case CONSTRUCTOR:
            case METHOD:
            case FUNCTION:
                parseSubroutineDec();
                break;
            case VAR:
                parseVarDec();
                break;
            default:
                break;
            }
        }
    }

    void parseClass() {
        String doc = getLast().doc();
        if (parseIdentifier()) {
            statics = 0;
            fields = 0;
            className = name;
            if (doc != null) {
                symbols.add(className, className + "$DOC", doc);
            }
        }
    }

    void parseClassVarDec() {
        TokenType kind = getLast().type();
        String doc = getLast().doc();
        if (!parseType()) {
            return;
        }
        do {
            if (!parseIdentifier()) {
                return;
            }
            if (kind == TokenType.STATIC) {
                symbols.add(className, "static" + statics, type);
                symbols.add(className, "static" + statics + "$NAME", name);
                if (doc != null) {
                    symbols.add(className, "static" + statics + "$DOC", doc);
                }
                statics++;
            } else {
                symbols.add(className, "field" + fields, type);
                symbols.add(className, "field" + fields + "$NAME", name);
                if (doc != null) {
                    symbols.add(className, "field" + statics + "$DOC", doc);
                }
                fields++;
            }
        } while (consume().type() == TokenType.COMMA);
        currentToken--; // reverse the last consumption in case there are no separation tokens
    }

    void parseSubroutineDec() {
        functionType = getLast().type().name();
        String doc = getLast().doc();
        if (!parseTypedName()) {
            return;
        }
        functionName = name;
        vars = 0;
        symbols.add(className + "." + functionName, "TYPE", functionType);
        symbols.add(className + "." + functionName, "RETURN", type);
        if (doc != null) {
            symbols.add(className, className + "." + functionName + "$DOC", doc);
        }
        if (peek().type() == TokenType.LEFT_PAREN) {
            consume();
            parseParameterList();
        }
        // ignore following tokens
    }

    void parseParameterList() {
        int i = 0;
        if (functionType.equals("METHOD")) {
            i = 1;
        }
        if (peek().type() != TokenType.RIGHT_PAREN) { // parameter list
            if (!parseTypedName()) {
                return;
            }
            symbols.add(className + "." + functionName, "arg" + i, type);
            symbols.add(className + "." + functionName, "arg" + i + "$NAME", name);
            while (peek().type() == TokenType.COMMA) {
                consume(); // ,
                i++;
                if (!parseTypedName()) {
                    return;
                }
                symbols.add(className + "." + functionName, "arg" + i, type);
                symbols.add(className + "." + functionName, "arg" + i + "$NAME", name);
            }
        }
        // ignore following tokens
    }

    void parseVarDec() {
        if (!parseType()) {
            return;
        }
        do {
            if (!parseIdentifier()) {
                return;
            }
            symbols.add(className + "." + functionName, "local" + vars, type);
            symbols.add(className + "." + functionName, "local" + vars + "$NAME", name);
            vars++;
        } while (consume().type() == TokenType.COMMA);
        currentToken--; // reverse the last consumption in case there are no separation tokens
    }
}
