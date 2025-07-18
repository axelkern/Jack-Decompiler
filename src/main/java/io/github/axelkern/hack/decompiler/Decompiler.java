package io.github.axelkern.hack.decompiler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

import io.github.axelkern.hack.util.Util;
import io.github.axelkern.hack.util.Version;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Decompiles VM code into Jack source code.
 * 
 * @author Axel Kern
 */
@Command(name = "decompiler", mixinStandardHelpOptions = true, versionProvider = Decompiler.VersionProvider.class, description = "Decompiles VM code into Jack source code", sortOptions = false)
public class Decompiler implements Callable<Integer> {
    private static final String PROGRAM_NAME = "Jack Decompiler " + Version.get(Decompiler.class);

    static class VersionProvider implements IVersionProvider {
        @Override
        public String[] getVersion() throws Exception {
            return new String[] { PROGRAM_NAME };
        }
    }

    @Parameters(index = "0", paramLabel = "<path>", description = "The file or directory to be decompiled")
    static Path path;
    @Option(names = {
            "--overwrite" }, negatable = false, defaultValue = "false", description = "Overwrite existing .jack files")
    static boolean overwrite;
    @Option(names = {
            "--no-rename" }, negatable = false, defaultValue = "false", description = "Name all variables based on memory segment")
    static boolean keepVarNames;
    @Option(names = {
            "--char-as-int" }, negatable = false, defaultValue = "false", description = "Always use int type instead of char")
    static boolean charAsInt;
    @Option(names = {
            "--no-force-char" }, negatable = true, defaultValue = "true", description = "Forces char type if compared to another char")
    static boolean forceChar;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Decompiler()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        io.github.axelkern.hack.util.Version.print("Jack Decompiler");
        List<Path> files = Util.getFileList(path, ".vm");
        if (files.size() == 0) {
            System.err.println("File or directory not found");
            return 1;
        }
        DecompilationEngine decompiler = new DecompilationEngine();
        for (int pass = 3; pass >= 0; pass--) {
            final int passNum = pass;
            files.forEach(file -> {
                String className = file.getFileName().toString();
                if (className.indexOf('.') >= 0) {
                    className = className.substring(0, className.lastIndexOf('.'));
                }
                if (passNum > 0) { // analyzing
                    decompiler.analyze(className, Util.readFileAsList(file));
                } else { // decompiling
                    System.out.println("Decompiling " + file.getFileName());
                    String outputFileName = file.toString();
                    outputFileName = outputFileName.substring(0, outputFileName.lastIndexOf(".vm")) + ".jack";
                    if (!overwrite && Files.exists(Paths.get(outputFileName))) {
                        System.err.println("Skipped existing file " + Paths.get(outputFileName).getFileName());
                    } else {
                        List<String> jackSource = decompiler.decompile(className, Util.readFileAsList(file));
                        Util.writeFile(outputFileName, jackSource);
                    }
                }
            });
        }
        return 0;
    }
}
