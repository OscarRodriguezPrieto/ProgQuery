package es.uniovi.reflection.progquery.task_result;


import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CompilationResult {

    private boolean errorBeforeTask;
    private final int totalJavaFiles;
    private final List<Diagnostic<? extends JavaFileObject>> errors;

    public CompilationResult(int totalJavaFiles, List<Diagnostic<? extends JavaFileObject>> errors) {
        this.totalJavaFiles = totalJavaFiles;
        this.errors = errors;
        errorBeforeTask = false;
    }

    public CompilationResult() {
        this.totalJavaFiles = 0;
        this.errors = new ArrayList<>();
        errorBeforeTask = false;
    }

    public CompilationResult(boolean errorBeforeTask) {
        this.totalJavaFiles = 0;
        this.errors = new ArrayList<>();
        this.errorBeforeTask = errorBeforeTask;
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

    public boolean compilationSuccess() {
        return totalJavaFiles > 0 && compilationErrors().count() == 0;
    }
}
