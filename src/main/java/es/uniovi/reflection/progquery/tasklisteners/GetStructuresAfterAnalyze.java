package es.uniovi.reflection.progquery.tasklisteners;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.cache.DefinitionCache;
import es.uniovi.reflection.progquery.cache.MultiModuleDefinitionCache;
import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.manager.NEO4JManager;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.relations.PGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.PartialRelation;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.typeInfo.PackageInfo;
import es.uniovi.reflection.progquery.utils.GraphUtils;
import es.uniovi.reflection.progquery.utils.JavacInfo;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.visitors.ASTTypesVisitor;
import es.uniovi.reflection.progquery.visitors.PDGProcessing;

import javax.tools.JavaFileObject;
import java.util.HashMap;
import java.util.Map;

public class GetStructuresAfterAnalyze implements TaskListener {
    private static final boolean DEBUG = false;
    private final JavacTask task;
    private Map<JavaFileObject, Integer> classCounter = new HashMap<JavaFileObject, Integer>();

    // private Set<CompilationUnitTree> unitsInTheSameFile = new
    // HashSet<CompilationUnitTree>();
    private boolean started = false;
    private Pair<PartialRelation<RelationTypes>, Object> argument;


    private PDGProcessing pdgUtils = new PDGProcessing();
    private ASTAuxiliarStorage ast = new ASTAuxiliarStorage();
    // private final GraphDatabaseService graphDb;
    private static final boolean MERGING_ALLOWED = true;


    //CALLABLE_DEF, isDeclared:true, accessLevel: 'public' or 'protected' and not final
    //TYPE_DEFINITION, isDeclared:true, accessLevel: 'public' or 'protected' and not final
    //TYPE_NODE AND NOT AST_TYPE AND isDeclared:false
    public GetStructuresAfterAnalyze(JavacTask task, String programID, String userID) {
        this.task = task;
        DatabaseFachade.CURRENT_INSERTION_STRATEGY.startAnalysis();
        if (MERGING_ALLOWED) {
            NodeWrapper retrievedProgram;
            try (NEO4JManager manager = DatabaseFachade.CURRENT_INSERTION_STRATEGY.getNewManager()) {
                retrievedProgram = manager.getProgramFromDB(programID, userID);
            }
            if (retrievedProgram != null) {
                DatabaseFachade.CURRENT_INSERTION_STRATEGY.newMultiModuleProject();
                MultiModuleDefinitionCache.initExternalCache(programID,userID);
                PackageInfo.setCurrentProgram(retrievedProgram);
                return;
            }

        }
        DefinitionCache.TYPE_CACHE = new DefinitionCache<>();
        DefinitionCache.METHOD_DEF_CACHE = new DefinitionCache<>();
        PackageInfo.createCurrentProgram(programID, userID);
    }


    @Override
    public void finished(TaskEvent arg0) {

        if (DEBUG)
            System.out.println("FINISHING  FOR " + arg0.getSourceFile() + "( " +
                    (arg0.getSourceFile() == null ? "" : arg0.getSourceFile().getName()) + " ) " + arg0.getKind());
        CompilationUnitTree cuTree = arg0.getCompilationUnit();

        if (arg0.getKind() == Kind.PARSE)
            classCounter.put(cuTree.getSourceFile(), cuTree.getTypeDecls().size());
        else if (arg0.getKind() == Kind.ANALYZE) {

            started = true;
            int currentTypeCounter = classCounter.get(cuTree.getSourceFile());
            if (cuTree.getTypeDecls().size() == 0)
                firstScanIfNoTypeDecls(cuTree);
            else {
                boolean firstClass = classCounter.get(cuTree.getSourceFile()) == cuTree.getTypeDecls().size();
                int nextTypeDecIndex = 0;
                if (cuTree.getTypeDecls().size() > 1) {

                    String[] tydcSplit = arg0.getTypeElement().toString().split("\\.");
                    String simpleTypeName =
                            tydcSplit.length > 0 ? tydcSplit[tydcSplit.length - 1] : arg0.getTypeElement().toString();
                    boolean found = false;
                    for (int i = 0; i < cuTree.getTypeDecls().size(); i++) {
                        if (cuTree.getTypeDecls().get(i) instanceof JCTree.JCSkip) {
                            if (firstClass) {
                                GraphUtils.connectWithParent(DatabaseFachade.CURRENT_DB_FACHADE
                                                .createSkeletonNode(cuTree.getTypeDecls().get(i),
                                                        NodeTypes.EMPTY_STATEMENT),
                                        argument.getFirst().getStartingNode(), RelationTypes.ENCLOSES);
                                currentTypeCounter--;
                            }
                            continue;
                        }
                        if (((ClassTree) cuTree.getTypeDecls().get(i)).getSimpleName().contentEquals(simpleTypeName)) {
                            nextTypeDecIndex = i;
                            found = true;
                            if (!firstClass)
                                break;
                        }
                    }
                    if (!found)
                        throw new IllegalStateException(
                                "NO TYPE DEC FOUND IN CU MATCHING JAVAC CURRENT " + simpleTypeName);
                }
                classCounter.put(cuTree.getSourceFile(), --currentTypeCounter);

                if (firstClass)
                    firstScan(cuTree, cuTree.getTypeDecls().get(nextTypeDecIndex));
                else
                    scan((ClassTree) cuTree.getTypeDecls().get(nextTypeDecIndex), false, cuTree);

            }

            if (currentTypeCounter <= 0)
                classCounter.remove(cuTree.getSourceFile());


        }
        if (DEBUG)
            System.out.println("FINISHED FOR " + arg0.getSourceFile() + "( " +
                    (arg0.getSourceFile() == null ? "" : arg0.getSourceFile().getName()) + " ) " + arg0.getKind());
    }

    private void firstScanIfNoTypeDecls(CompilationUnitTree u) {
        //		System.out.println("BEFORE SETTING JAVAC INFO");
        JavacInfo.setJavacInfo(new JavacInfo(u, task));
        //		System.out.println("AFTER SETTING JAVAC INFO");
        String fileName = u.getSourceFile().toUri().toString();
        // transaction = DatabaseFachade.beginTx();

        NodeWrapper compilationUnitNode =
                DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(u, NodeTypes.COMPILATION_UNIT);

        addPackageInfo(((JCCompilationUnit) u).packge, compilationUnitNode);
        compilationUnitNode.setProperty("fileName", fileName);

        argument = Pair.createPair(compilationUnitNode, null);
        //		System.out.println("FIRST SCAN WITH NO TYPE DECS");
    }

    private NodeWrapper addPackageInfo(Symbol currentPackage, NodeWrapper compilationUnitNode) {

        PackageInfo.PACKAGE_INFO.currentPackage = currentPackage;
        NodeWrapper packageNode = PackageInfo.PACKAGE_INFO.putDeclaredPackage(currentPackage);
        packageNode.createRelationshipTo(compilationUnitNode, PGRelationTypes.PACKAGE_HAS_COMPILATION_UNIT);
        // packageNode.setProperty("isDeclared", true);
        return packageNode;
    }

    private void firstScan(CompilationUnitTree cu, Tree typeDeclaration) {
        JavacInfo.setJavacInfo(new JavacInfo(cu, task));
        String fileName = cu.getSourceFile().getName();
        NodeWrapper compilationUnitNode =
                DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(cu, NodeTypes.COMPILATION_UNIT);
        addPackageInfo(((JCCompilationUnit) cu).packge, compilationUnitNode);
        compilationUnitNode.setProperty("fileName", fileName);

        argument = Pair.createPair(compilationUnitNode, null);
        if (typeDeclaration instanceof ModuleTree)
            return;
        scan((ClassTree) typeDeclaration, true, cu);

    }

    private void scan(ClassTree typeDeclaration, boolean first, CompilationUnitTree cu) {

        DefinitionCache.ast = ast;
        new ASTTypesVisitor(typeDeclaration, first, pdgUtils, ast, argument.getFirst().getStartingNode())
                .scan(cu, argument);
    }

    @Override
    public void started(TaskEvent arg0) {

        if (DEBUG)
            System.out.println("STARTING FOR " + arg0.getSourceFile() + "( " +
                    (arg0.getSourceFile() == null ? "" : arg0.getSourceFile().getName()) + " ) " + arg0.getKind());
        if (arg0.getKind() == Kind.GENERATE && started)
            if (classCounter.size() == 0) {
                if (DEBUG)
                    System.out.println("STARTING CREATING ATTRS NOT DECLARED");
                pdgUtils.createNotDeclaredAttrRels(ast);

                if (DEBUG)
                    System.out.println("STARTING CREATING STORE PACKAGES DEPS");
                createStoredPackageDeps();

                if (DEBUG)
                    System.out.println("STARTING DYNAMIC METHOD CALL");

                dynamicMethodCallAnalysis();

                if (DEBUG)
                    System.out.println("STARTING INTERPROCEDRAL MUTABILITY");
                interproceduralPDGAnalysis();

                if (DEBUG)
                    System.out.println("STARTING INITIALIZATION ANALYSIS");
                initializationAnalysis();

                shutdownDatabase();
                started = false;
            }

        if (DEBUG)
            System.out.println("STARTED FOR " + arg0.getSourceFile() + "( " +
                    (arg0.getSourceFile() == null ? "" : arg0.getSourceFile().getName()) + " ) " + arg0.getKind());

    }

    private void createStoredPackageDeps() {
        PackageInfo.PACKAGE_INFO.createStoredPackageDeps();

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
        if (DEBUG)
            System.out.println("STARTING SHUTDOWN THE DATABASE");

        DatabaseFachade.CURRENT_INSERTION_STRATEGY.endAnalysis();
        if (DEBUG)
            System.out.println("SHUTDOWN THE DATABASE ENDED");

    }

}
