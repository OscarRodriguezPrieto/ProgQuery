package es.uniovi.reflection.progquery.task_result;


import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;

public class ModuleStats {

    private final int totalJavaFiles;
    private final List<Diagnostic<? extends JavaFileObject>> errors;

    public ModuleStats(int totalJavaFiles, List<Diagnostic<? extends JavaFileObject>> errors) {
        this.totalJavaFiles = totalJavaFiles;
        this.errors = errors;
    }

    public ModuleStats() {
        this.totalJavaFiles = 0;
        this.errors = new ArrayList<>();
    }

    public int getTotalJavaFiles() {
        return totalJavaFiles;
    }

    public List<Diagnostic<? extends JavaFileObject>> getErrors() {
        return errors;
    }

    public double coverage() {
        return 1.0 - errors.stream().filter(error -> error.getKind() == Diagnostic.Kind.ERROR).count() * 1.0 / totalJavaFiles;
    }


}
