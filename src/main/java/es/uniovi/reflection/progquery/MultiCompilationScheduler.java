package es.uniovi.reflection.progquery;

import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.JavacTaskImpl;
import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.manager.NEO4JManager;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.task_result.CompilationResult;
import es.uniovi.reflection.progquery.tasklisteners.GetStructuresAfterAnalyze;
import es.uniovi.reflection.progquery.typeInfo.PackageInfo;
import es.uniovi.reflection.progquery.visitors.PDGProcessing;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiCompilationScheduler {
    private PDGProcessing pdgUtils = new PDGProcessing();
    private ASTAuxiliarStorage ast = new ASTAuxiliarStorage();
    private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private int previousNodesInserted = 0, previousRelsInserted = 0;

    private static final boolean MERGING_ALLOWED = true;

    public MultiCompilationScheduler(String programID, String userID) {
        // this.graphDb = graphDb;
        DatabaseFachade.CURRENT_INSERTION_STRATEGY.startAnalysis();
        if (MERGING_ALLOWED) {
            NodeWrapper retrievedProgram = null;
            try (NEO4JManager manager = DatabaseFachade.CURRENT_INSERTION_STRATEGY.getManager()) {
                retrievedProgram = manager.getProgramFromDB(programID, userID);
            }
            if (retrievedProgram != null) {
                PackageInfo.setCurrentProgram(retrievedProgram);
                return;
            }

        }
        PackageInfo.createCurrentProgram(programID, userID);
    }

    public CompilationResult newCompilationTask(String sourcePath, String classPath, String javacSourceV,
                                                String javacTargetV, List<JavaFileObject> excludedSources,
                                                List<String> compilerArgs, String... sourceFileDirs) {

        String firstSOurceDir = sourceFileDirs[0];
        System.out.println("NEW TASK on " + firstSOurceDir + ":");
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, Charset.forName("UTF-8"));
        List<File> files = new ArrayList<>();
        for (String fileDir : sourceFileDirs)
            files.addAll(listSourceFiles(fileDir));

        List<JavaFileObject> sources = new ArrayList<>();
        fileManager.getJavaFileObjectsFromFiles(files).iterator().forEachRemaining(sources::add);
        int sourceFilesCount = sources.size();
        sources.removeAll(excludedSources);
        if (sources.size() == 0)
            return new CompilationResult(firstSOurceDir);

        List<String> compilerOptions = new ArrayList<>(
                Arrays.asList("-nowarn", "-d", Paths.get(sourcePath, "target", "classes").toAbsolutePath().toString(),
                        "-target", javacTargetV, "-source", javacSourceV, "-classpath", classPath));
        compilerOptions.addAll(compilerArgs);
        //        if (javacSourceV >= 15)
        //            compilerOptions.add("--enable-preview");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavacTaskImpl compilerTask =
                (JavacTaskImpl) compiler.getTask(null, null, diagnostics, compilerOptions, null, sources);
        runPQCompilationTask(compilerTask);
        showErrors(diagnostics);

        CompilationResult result =
                new CompilationResult(firstSOurceDir, sourceFilesCount, diagnostics.getDiagnostics(), sources.size(),
                        InfoToInsert.INFO_TO_INSERT.getNodeSet().size() - previousNodesInserted,
                        InfoToInsert.INFO_TO_INSERT.getRelSet().size() - previousRelsInserted);
        this.previousNodesInserted = InfoToInsert.INFO_TO_INSERT.getNodeSet().size();
        this.previousRelsInserted = InfoToInsert.INFO_TO_INSERT.getRelSet().size();
        return result;
    }


    public CompilationResult newCompilationTask(String sourcePath, String classPath, String javacSourceV,
                                                String javacTargetV) {
        return newCompilationTask(sourcePath, classPath, javacSourceV, javacTargetV, new ArrayList<>(),
                new ArrayList<>(), "");
    }

    public static List<File> listSourceFiles(String path) {
        // We want to find only regular files
        final String JAVA_FILE_CLUE = ".java";
        final String MODULE_INFO_CLUE = "module-info.java";
        final Path TARGET_CLASSES_PATH = Paths.get(path, "target", "classes").toAbsolutePath();
        return listFiles(path, f -> f.getFileName().toString().endsWith(JAVA_FILE_CLUE) &&
                !f.getFileName().endsWith(MODULE_INFO_CLUE) && !f.toAbsolutePath().startsWith(TARGET_CLASSES_PATH));

    }

    public static List<File> listFiles(String path, Predicate<Path> conditionToAdd) {
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            return walk.filter(Files::isRegularFile).filter(conditionToAdd).map(f -> f.toAbsolutePath().toFile())
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void addListener(JavacTask compilerTask) {
        GetStructuresAfterAnalyze pqListener = new GetStructuresAfterAnalyze(compilerTask, this);
        compilerTask.addTaskListener(pqListener);
    }

    public void runPQCompilationTask(JavacTask compilerTask) {
        addListener(compilerTask);
        //        compilerTask.getTaskListeners().forEach(System.out::println);

        compilerTask.call();
        System.out.println("NODEsET:" + InfoToInsert.INFO_TO_INSERT.getNodeSet().size());
        System.out.println("RELsET:" + InfoToInsert.INFO_TO_INSERT.getRelSet().size());
    }

    public PDGProcessing getPdgUtils() {
        return pdgUtils;
    }

    public ASTAuxiliarStorage getAst() {
        return ast;
    }

    public void endAnalysis() {
        pdgUtils.createNotDeclaredAttrRels(ast);

        createStoredPackageDeps();

        dynamicMethodCallAnalysis();

        interproceduralPDGAnalysis();

        initializationAnalysis();

        shutdownDatabase();

    }

    private void showErrors(DiagnosticCollector<JavaFileObject> diagnostics) {
        if (diagnostics.getDiagnostics().size() > 0) {
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                System.err
                        .format("Error on [%d,%d] in %s %s\n", diagnostic.getLineNumber(), diagnostic.getColumnNumber(),
                                diagnostic.getSource(), diagnostic.getMessage(null));
            }
        }
    }


    private void createStoredPackageDeps() {

        // Transaction transaction = DatabaseFachade.beginTx();
        PackageInfo.PACKAGE_INFO.createStoredPackageDeps();
        // transaction.success();
        // transaction.close();

    }

    private void createAllParamsToMethodsPDGRels() {
        ast.createAllParamsToMethodsPDGRels();
    }

    private void initializationAnalysis() {
        ast.doInitializationAnalysis();
    }

    private void interproceduralPDGAnalysis() {
        ast.doInterproceduralPDGAnalysis();
        createAllParamsToMethodsPDGRels();
    }

    private void dynamicMethodCallAnalysis() {
        ast.doDynamicMethodCallAnalysis();
    }

    public void shutdownDatabase() {

        DatabaseFachade.CURRENT_INSERTION_STRATEGY.endAnalysis();

    }
}
