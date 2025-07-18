package io.github.axelkern.hack.decompiler;

import java.util.HashMap;
import java.util.Map;

/**
 * Symbol table that stores simple <String, String> pairs, but offers some
 * specialized access methods to support the decompiler.<br>
 * <br>
 * Key convention:<br>
 * statics, fields: className$varName<br>
 * return types: functionName$RETURN<br>
 * function call types: functionName$TYPE<br>
 * locals, args: functionName$varName<br>
 * number of statics, fields: className$STATICS, className$FIELDS number of
 * locals, args: functionName$LOCALS, functionName$ARGS
 */
class SymbolTable {
    private Map<String, String> table = new HashMap<>();

    boolean contains(String key1, String key2) {
        return get(key1, key2) != null;
    }

    boolean contains(String className, String functionName, String identifier) {
        return contains(getPrimaryKey(className, functionName, identifier), identifier);
    }

    String get(String key1, String key2) {
        if (table.containsKey(key1 + "$" + key2)) {
            return table.get(key1 + "$" + key2);
        } else {
            return null;
        }
    }

    String get(String className, String functionName, String identifier) {
        return get(getPrimaryKey(className, functionName, identifier), identifier);
    }

    /**
     * Same as get() but will return "" instead of null if no entry matching was
     * found.
     */
    String find(String key1, String key2) {
        String result = get(key1, key2);
        if (result == null) {
            return "";
        } else {
            return result;
        }
    }

    /**
     * Same as get() but will return "" instead of null if no entry matching was
     * found.
     */
    String find(String className, String functionName, String identifier) {
        return find(getPrimaryKey(className, functionName, identifier), identifier);
    }

    void add(String key1, String key2, String value) {
        table.put(key1 + "$" + key2, value);
    }

    void add(String className, String functionName, String identifier, String value) {
        add(getPrimaryKey(className, functionName, identifier), identifier, value);
    }

    /**
     * Selects the correct primary key depending on the identifier
     * 
     * @param className    Name of the current class
     * @param functionName Name of the current function
     * @param identifier   Variable name or entry name (i.e. static2 or FIELDS)
     * @return Returns the primary key under which the entries for the specified
     *         identifier are stored.
     */
    private static String getPrimaryKey(String className, String functionName, String identifier) {
        identifier = identifier.toUpperCase();
        if (identifier.startsWith("STATIC") || identifier.startsWith("FIELD") || identifier.startsWith("THIS")) {
            return className;
        } else {
            return functionName;
        }
    }

    int size() {
        return table.size();
    }
}
