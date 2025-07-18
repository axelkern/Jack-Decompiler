module io.github.axelkern.nand2tetris {
    requires java.desktop;
    requires java.compiler;
    requires java.logging;
    requires java.prefs;
    requires info.picocli;

    opens io.github.axelkern.hack.jackcompiler to info.picocli;
    opens io.github.axelkern.hack.decompiler to info.picocli;
    opens io.github.axelkern.hack.util to info.picocli;

}