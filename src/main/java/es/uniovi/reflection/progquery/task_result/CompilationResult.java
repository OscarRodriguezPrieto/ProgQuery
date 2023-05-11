package es.uniovi.reflection.progquery.task_result;


import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompilationResult {

    private String sourcePath;
    private final int totalJavaFiles;
    private final int totalFilesToCompile;
    private final List<Diagnostic<? extends JavaFileObject>> errors;
    private int compilationTries = 1;
    private final int insertedNodes, insertedRels;

    public CompilationResult(String sourcePath, int totalJavaFiles, List<Diagnostic<? extends JavaFileObject>> errors,
                             int totalFilesToCompile, int insertedNodes, int insertedRels) {
        this.sourcePath = sourcePath;
        this.totalJavaFiles = totalJavaFiles;
        this.errors = errors;
        this.totalFilesToCompile = totalFilesToCompile;
        this.insertedNodes = insertedNodes;
        this.insertedRels = insertedRels;
    }

    public CompilationResult(String sourcePath) {
        this.sourcePath = sourcePath;
        this.totalJavaFiles = 0;
        this.errors = new ArrayList<>();
        totalFilesToCompile = 0;
        insertedNodes = 0;
        insertedRels = 0;
    }

    public int getTotalJavaFiles() {
        return totalJavaFiles;
    }

    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return errors;
    }

    public double coverage() {
        return 1.0 - errors.stream().filter(error -> error.getKind() == Diagnostic.Kind.ERROR).count() * 1.0 /
                totalJavaFiles;
    }

    public Stream<Diagnostic<? extends JavaFileObject>> compilationErrors() {
        return errors.stream().filter(error -> error.getKind() == Diagnostic.Kind.ERROR);
    }

    public List<JavaFileObject> getFilesWithErrors() {
        return compilationErrors().map(error -> error.getSource()).collect(Collectors.toList());
    }

    public boolean compilationSuccess() {
        return totalJavaFiles > 0 && compilationErrors().count() == 0;
    }


    public String toString(String moduleName) {
        String detail = String.format("%d nodes and %d rels. inserted.", insertedNodes, insertedRels);
        if (compilationTries > 1)
            detail += String.format("(after %d compilation tries, excluding %d files)", compilationTries,
                    totalJavaFiles - totalFilesToCompile);

        final long compilationErrors = compilationErrors().count();
        if (compilationErrors == 0)
            return String.format("Module %s (%s) with %d Java files compiled successfully. ", moduleName, sourcePath,
                    getTotalJavaFiles()) + detail;
        return String.format("Module %s (%s) with %d Java files generated %d compilation errors so it could not be " +
                "compiled.", moduleName, sourcePath, getTotalJavaFiles(), compilationErrors) + detail;
    }

    public void setCompilationTries(int compilationTries) {
        this.compilationTries = compilationTries;
    }

    public int getTotalFilesToCompile() {
        return totalFilesToCompile;
    }

}
