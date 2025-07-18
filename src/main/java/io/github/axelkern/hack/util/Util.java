package io.github.axelkern.hack.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {
    static {
        try (InputStream is = Util.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().reset();
        } catch (IOException e) {
            LogManager.getLogManager().reset();
        }
    }
    public static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    public static String getOutputFileName(Path path, String extension) {
        return getOutputFileName(path.toFile(), extension);
    }

    public static String getOutputFileName(File file, String extension) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            error("Invalid file name");
        }
        String fileName = file.getPath();
        if (file.isDirectory()) {
            if (!fileName.endsWith(File.separator)) {
                fileName = fileName + File.separator;
            }
            return fileName + file.getName() + extension;
        } else {
            if (fileName.contains(".")) {
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            }
            return fileName + extension;
        }
    }

    public static String readFileAsString(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            error("Error reading " + file);
            return null;
        }
    }

    public static List<String> readFileAsList(Path file) {
        return splitStringToList(readFileAsString(file));
    }

    public static void writeFile(String fileName, List<String> lines) {
        assert lines != null;
        try (BufferedWriter output = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : lines) {
                output.write(line);
                output.newLine();
            }
        } catch (IOException e) {
            error("Error writing " + fileName);
        }
    }

    public static void writeFile(String fileName, String s) {
        assert s != null;
        try (BufferedWriter output = new BufferedWriter(new FileWriter(fileName))) {
            output.write(s);
        } catch (IOException e) {
            error("Error writing " + fileName);
        }
    }

    public static List<String> splitStringToList(String s) {
        return new ArrayList<>(s.lines().toList());
    }

    public static String joinListToString(List<String> list) {

        return String.join(System.lineSeparator(), list);
    }

    public static void error(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    public static List<Path> getFileList(Path path, String extension) {
        List<Path> files;
        if (Files.isDirectory(path)) {
            try {
                return Files.list(path).filter(Files::isRegularFile).filter(file -> file.toString().endsWith(extension))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                return new ArrayList<>();
            }
        } else {
            files = new ArrayList<>();
            if (Files.isRegularFile(path)) {
                files.add(path);
            }
            return files;
        }
    }

    /**
     * Compares two Records, ignoring fields containing a null value.
     * 
     * @param a The first Record to be compared.
     * @param b The second Record to be compared.
     * @return true if all non-null fields of the two specified Records are equal.
     */
    public static boolean matchRecords(Record a, Record b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;
        if (a.getClass() != b.getClass())
            return false;
        RecordComponent[] fields = a.getClass().getRecordComponents();
        for (int i = 0; i < fields.length; i++) {
            try {
                Object aValue = fields[i].getAccessor().invoke(a);
                if (aValue != null) {
                    Object bValue = fields[i].getAccessor().invoke(b);
                    if (bValue != null) {
                        if (!aValue.equals(bValue)) {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static String getJarLocation() {
        try {
            return new File(Util.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
                    .getParentFile().getPath();
        } catch (Exception e) {
            return ".";
        }
    }

    /**
     * Finds an object inside a list by its reference. It uses a comparison with the
     * == operator instead of equals. Therefore it will return the index of the
     * exact same object in the list or -1 if the object is not included in the
     * list.
     * 
     * @param l the list to be searched.
     * @param o the object to search for.
     * @return the index of the object in the list or -1 if it wasn't found.
     */
    public static int indexOfRef(@SuppressWarnings("rawtypes") List l, Object o) {
        for (int i = 0; i < l.size(); i++) {
            if (o == l.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public static String stripComments(String s) {
        int i = s.indexOf("//");
        if (i == 0) {
            return "";
        } else if (i > 0) {
            return s.substring(0, i - 1).strip();
        } else {
            return s.strip();
        }
    }

    public static <R extends Record> R cloneRecord(R template) {
        try {
            var types = new ArrayList<Class<?>>();
            var values = new ArrayList<>();
            for (var component : template.getClass().getRecordComponents()) {
                types.add(component.getType());
                values.add(component.getAccessor().invoke(template));
            }
            var canonical = template.getClass().getDeclaredConstructor(types.toArray(Class[]::new));
            @SuppressWarnings("unchecked")
            var result = (R) canonical.newInstance(values.toArray(Object[]::new));
            return result;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Reflection failed: " + e, e);
        }
    }

    /**
     * Finds the n-th occurrence of a substring in a string.
     * 
     * @param str    the string that is searched
     * @param substr the substring to search for
     * @param n      n-th occurrence to search for (starting with index 0 for the
     *               first occurrence)
     * @return the character position inside the string where the n-th occurrence
     *         was found or -1 if there aren't n occurrences of the specified
     *         substring
     */
    public static int ordinalIndexOf(String str, String substr, int n) {
        int pos = -1;
        do {
            pos = str.indexOf(substr, pos + 1);
        } while (n-- > 0 && pos != -1);
        return pos;
    }

    public static final Pattern FUNCTION_IDENTIFIER = Pattern
            .compile("[A-Za-z_][A-Za-z_0-9]*\\.[A-Za-z_][A-Za-z_0-9]*");
    public static final Pattern STATIC_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z_0-9]*\\.[0-9]*");

    public static boolean isFunctionSymbol(String symbol) {
        return FUNCTION_IDENTIFIER.matcher(symbol).matches();
    }

    public static boolean isStaticSymbol(String symbol) {
        return STATIC_IDENTIFIER.matcher(symbol).matches();
    }

    private static final String[] units = new String[] { "", "k", "M", "G", "T", "P", "E" };

    public static String getPrefixNotation(long value, int decimals) {
        long absolute = value == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(value);
        int unitIndex = ((int) Math.log10(absolute)) / 3;
        return String.format("%." + decimals + "f" + units[unitIndex], 1.0 * value / (Math.pow(10, unitIndex * 3)));
    }

    public static boolean isPowerOfTwo(int n) {
        return (n != 0) && ((n & (n - 1)) == 0);
    }
}
