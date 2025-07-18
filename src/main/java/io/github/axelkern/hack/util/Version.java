package io.github.axelkern.hack.util;

public class Version {
    public static final String APPLICATION = "Hack Compiler Collection";
    public static final String AUTHOR = "Axel Kern";
    public static final int YEAR = 2024;

    public static <T> String get(Class<T> c) {
        String version = c.getPackage().getImplementationVersion();
        if (version == null) {
            return "[dev]";
        } else {
            int pos = version.indexOf('-');
            if (pos == -1) {
                return version;
            } else {
                return version.substring(0, version.indexOf('-'));
            }
        }
    }

    public static void print() {
        print(null);
    }

    public static void print(String module) {
        System.out.print(APPLICATION + " (created " + YEAR + " by " + AUTHOR + ")");
        if (module != null) {
            System.out.println(" - " + module);
        } else {
            System.out.println();
        }
    }
}
