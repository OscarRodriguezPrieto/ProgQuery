package es.uniovi.reflection.progquery.task_result;


import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompilationResult {

    private String sourcePath;
    private boolean errorBeforeTask;
    private final int totalJavaFiles;
    private final int totalFilesToCompile;
    private final List<Diagnostic<? extends JavaFileObject>> errors;
    private int compilationTries = 1;

    public CompilationResult(String sourcePath, int totalJavaFiles, List<Diagnostic<? extends JavaFileObject>> errors,
                             int totalFilesToCompile) {
        this.sourcePath = sourcePath;
        this.totalJavaFiles = totalJavaFiles;
        this.errors = errors;
        errorBeforeTask = false;
        this.totalFilesToCompile = totalFilesToCompile;
    }

    public CompilationResult(String sourcePath) {
        this(sourcePath, false);
    }

    public CompilationResult(String sourcePath, boolean errorBeforeTask) {
        this.sourcePath = sourcePath;
        this.totalJavaFiles = 0;
        this.errors = new ArrayList<>();
        this.errorBeforeTask = errorBeforeTask;
        totalFilesToCompile = 0;
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

    public void setErrorBeforeTask(boolean errorBeforeTask) {
        this.errorBeforeTask = errorBeforeTask;
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

    public boolean isErrorBeforeTask() {
        return errorBeforeTask;
    }

    public String toString(String moduleName) {
        final String tries = compilationTries > 1 ?
                String.format("(after %d compilation tries, excluding %d files)", compilationTries,
                        totalJavaFiles - totalFilesToCompile) : "";
        String previousError = isErrorBeforeTask() ? " generated an error before compilation but it was " : "";
        final long compilationErrors = compilationErrors().count();
        if (compilationErrors == 0)
            return String
                    .format("Module %s with %d Java files %s compiled successfully.", moduleName, getTotalJavaFiles(),
                            previousError) + tries;
        previousError = isErrorBeforeTask() ? " generated an error before compilation, " : "";
        return String
                .format("Module %s with %d Java files %s generated %d compilation errors so it could not be compiled.",
                        moduleName, getTotalJavaFiles(), previousError, compilationErrors) + tries;
    }

    public void setCompilationTries(int compilationTries) {
        this.compilationTries = compilationTries;
    }

    public int getTotalFilesToCompile() {
        return totalFilesToCompile;
    }

}
