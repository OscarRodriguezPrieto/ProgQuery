package es.uniovi.reflection.progquery;

import com.sun.source.util.JavacTask;
import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.manager.NEO4JManager;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.tasklisteners.GetStructuresAfterAnalyze;
import es.uniovi.reflection.progquery.typeInfo.PackageInfo;
import es.uniovi.reflection.progquery.visitors.PDGProcessing;

public class MultiCompilationScheduler {
    private PDGProcessing pdgUtils = new PDGProcessing();
    private ASTAuxiliarStorage ast = new ASTAuxiliarStorage();

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
    public void newCompilationTask(JavacTask compilerTask){
        GetStructuresAfterAnalyze pqListener=new GetStructuresAfterAnalyze(compilerTask, this);
        compilerTask.addTaskListener(pqListener);
        System.out.println("NEW TASK:");
//        compilerTask.getTaskListeners().forEach(System.out::println);

        compilerTask.call();
        System.out.println("NODEsET:"+ InfoToInsert.INFO_TO_INSERT.getNodeSet().size());
        System.out.println("RELsET:"+InfoToInsert.INFO_TO_INSERT.getRelSet().size());
    }

    public PDGProcessing getPdgUtils() {
        return pdgUtils;
    }

    public ASTAuxiliarStorage getAst() {
        return ast;
    }

    public void endAnalysis(){
        pdgUtils.createNotDeclaredAttrRels(ast);

        createStoredPackageDeps();

        dynamicMethodCallAnalysis();

        interproceduralPDGAnalysis();

        initializationAnalysis();

        shutdownDatabase();
    }


    private void createStoredPackageDeps() {

        // Transaction transaction = DatabaseFachade.beginTx();
        PackageInfo.PACKAGE_INFO.createStoredPackageDeps();
        // transaction.success();
        // transaction.close();

    }

    private void createAllParamsToMethodsPDGRels() {

        // Transaction transaction = DatabaseFachade.beginTx();
        ast.createAllParamsToMethodsPDGRels();
        // transaction.success();
        // transaction.close();
    }

    private void initializationAnalysis() {

        // Transaction transaction = DatabaseFachade.beginTx();
        ast.doInitializationAnalysis();
        // transaction.success();
        // transaction.close();
    }

    private void interproceduralPDGAnalysis() {

        // Transaction transaction = DatabaseFachade.beginTx();
        ast.doInterproceduralPDGAnalysis();
        // transaction.success();
        // transaction.close();

        createAllParamsToMethodsPDGRels();
    }

    private void dynamicMethodCallAnalysis() {
        // Transaction transaction = DatabaseFachade.beginTx();
        ast.doDynamicMethodCallAnalysis();
        // transaction.success();
        // transaction.close();
    }

    public void shutdownDatabase() {

        DatabaseFachade.CURRENT_INSERTION_STRATEGY.endAnalysis();

    }
}
