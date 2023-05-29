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
import es.uniovi.reflection.progquery.MultiCompilationScheduler;
import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.relations.CDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.PartialRelation;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.typeInfo.PackageInfo;
import es.uniovi.reflection.progquery.utils.GraphUtils;
import es.uniovi.reflection.progquery.utils.JavacInfo;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.visitors.ASTTypesVisitor;

import javax.tools.JavaFileObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GetStructuresAfterAnalyze implements TaskListener {
    private static final boolean DEBUG = false;
    private final JavacTask task;
    private Map<JavaFileObject, Integer> classCounter = new HashMap<JavaFileObject, Integer>();

    private MultiCompilationScheduler scheduler;
    private boolean started = false;
    private Pair<PartialRelation<RelationTypesInterface>, Object> argument;
    private final Set<JavaFileObject> sourcesToCompile;

    public GetStructuresAfterAnalyze(JavacTask task, MultiCompilationScheduler scheduler, Set<JavaFileObject> sources) {
        this.task = task;
        this.scheduler = scheduler;
        this.sourcesToCompile = new HashSet<>();
        this.sourcesToCompile.addAll(sources);
    }

    @Override
    public void finished(TaskEvent arg0) {

        if (DEBUG)
            System.out.println("FINISHING  FOR " + arg0.getSourceFile() + "( " +
                    (arg0.getSourceFile() == null ? "" : arg0.getSourceFile().getName()) + " ) " + arg0.getKind());
        CompilationUnitTree cuTree = arg0.getCompilationUnit();

        //Sources to compile may be null if PQ is used as plugin
        if (sourcesToCompile == null || sourcesToCompile.contains(cuTree.getSourceFile()))
            if (arg0.getKind() == Kind.PARSE)
                classCounter.put(cuTree.getSourceFile(), cuTree.getTypeDecls().size());
            else if (arg0.getKind() == Kind.ANALYZE) {

                //			System.out.println("FINISHING SCANNING CU " + cuTree.getSourceFile().getName() + " WITH "
                //					+ cuTree.getTypeDecls().size() + " TYPEDECS");
                //			System.out.println(arg0.getClass());
                //			System.out.println(arg0.getTypeElement());
                //			System.out.println(arg0.getTypeElement().toString());
                //			String[] tydcSplit = arg0.getTypeElement().toString().split("\\.");
                //			System.out.println(tydcSplit+" "+tydcSplit.length);
                started = true;
                int currentTypeCounter = classCounter.get(cuTree.getSourceFile());
                //            if (cuTree.getSourceFile().toString().contains
                //            ("C:\\Users\\Oskar\\Desktop\\investigacion\\post-doc\\pq_server_enterprise\\git_projects
                //            \\test_projects\\javassist\\src\\main\\javassist\\tools\\rmi\\StubGenerator.java")) {
                //            System.out.println("ACTUAL CU:"+cuTree.getSourceFile());
                //            System.out.println("N counter:" + currentTypeCounter);
                //                System.out.println("N typedecs:" + cuTree.getTypeDecls().size());

                //            }
                if (cuTree.getTypeDecls().size() == 0)
                    //				System.out.println("SCANNING CU " + cuTree.getSourceFile().getName() + " WITH 0
                    //				TYPEDECS");
                    firstScanIfNoTypeDecls(cuTree);
                else {

                    //				System.out.println("SCANNING CU " + cuTree.getSourceFile().getName() + " WITH "
                    //						+ cuTree.getTypeDecls().size() + " TYPEDECS");

                    boolean firstClass = classCounter.get(cuTree.getSourceFile()) == cuTree.getTypeDecls().size();
                    int nextTypeDecIndex = 0;
                    if (cuTree.getTypeDecls().size() > 1) {

                        String[] tydcSplit = arg0.getTypeElement().toString().split("\\.");
                        //					System.out.println(tydcSplit+" "+tydcSplit.length);
                        String simpleTypeName = tydcSplit.length > 0 ? tydcSplit[tydcSplit.length - 1] :
                                arg0.getTypeElement().toString();
                        //					System.out.println("JAVAC CURRENT SPLITTED TYPE NAME:" + simpleTypeName);
                        boolean found = false;
                        for (int i = 0; i < cuTree.getTypeDecls().size(); i++) {
                            if (cuTree.getTypeDecls().get(i) instanceof JCTree.JCSkip) {
                                if (firstClass) {
                                    GraphUtils.connectWithParent(DatabaseFachade.CURRENT_DB_FACHADE
                                                    .createSkeletonNode(cuTree.getTypeDecls().get(i),
                                                            NodeTypes.EMPTY_STATEMENT),
                                            argument.getFirst().getStartingNode(),
                                            RelationTypes.ENCLOSES);
                                    currentTypeCounter--;
                                }
                                continue;
                            }
                            //						System.out.println(((ClassTree) cuTree.getTypeDecls().get(i))
                            //						.getSimpleName());
                            if (((ClassTree) cuTree.getTypeDecls().get(i)).getSimpleName()
                                    .contentEquals(simpleTypeName)) {
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

                if (currentTypeCounter <= 0) {
                    classCounter.remove(cuTree.getSourceFile());
                    if (sourcesToCompile != null)
                        sourcesToCompile.remove(cuTree.getSourceFile());
                }

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
        packageNode.createRelationshipTo(compilationUnitNode, CDGRelationTypes.PACKAGE_HAS_COMPILATION_UNIT);
        // packageNode.setProperty("isDeclared", true);
        return packageNode;
    }

    private void firstScan(CompilationUnitTree cu, Tree typeDeclaration) {
        //    System.out.println("FIRST SCAN!");

        if (!JavacInfo.isInitialized())
            JavacInfo.setJavacInfo(new JavacInfo(cu, task));
        //		System.out.println("AFTER SETTING JAVAC INFOf");

        String fileName = cu.getSourceFile().getName();
        // transaction = DatabaseFachade.beginTx();

        // InsertionStrategy.CURRENT_INSERTION_STRATEGY.startAnalysis();

        NodeWrapper compilationUnitNode =
                DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(cu, NodeTypes.COMPILATION_UNIT);
        addPackageInfo(((JCCompilationUnit) cu).packge, compilationUnitNode);
        //System.out.println(fileName);
        compilationUnitNode.setProperty("fileName", fileName);

        argument = Pair.createPair(compilationUnitNode, null);
        //	System.out.println("BEFORE SCAN TYPEDEC\n"+u);
        if (typeDeclaration instanceof ModuleTree)
            return;
        scan((ClassTree) typeDeclaration, true, cu);
        //		System.out.println("AFTER SCAN TYPEDEC");

    }

    private void scan(ClassTree typeDeclaration, boolean first, CompilationUnitTree cu) {
        // if (DEBUG) {
        //		System.out.println("-*-*-*-*-*-*-* NEW TYPE DECLARATION AND VISITOR-*-*-*-*-*-*-*");
        //		System.out.println("CU:\t" + cu.getSourceFile().getName());
        // System.out.println("Final State:\n");
        //
        //		System.out.println("TYPE_DEC:\t" + ((JCClassDecl) typeDeclaration).sym);
        // }
        //        System.out.println("SCANING "+typeDeclaration.getSimpleName());
        //        System.out.println(cu.getSourceFile());
        //        System.out.println(typeDeclaration);
        //        System.out.println(cu.getClass());
        //        System.out.println(cu instanceof Serializable);

        new ASTTypesVisitor(typeDeclaration, first, scheduler.getPdgUtils(), scheduler.getAst(),
                argument.getFirst().getStartingNode()).scan(cu, argument);
    }

    @Override
    public void started(TaskEvent arg0) {

        if (DEBUG)
            System.out.println("STARTING FOR " + arg0.getSourceFile() + "( " +
                    (arg0.getSourceFile() == null ? "" : arg0.getSourceFile().getName()) + " ) " + arg0.getKind());
        if (arg0.getKind() == Kind.GENERATE && started)
            if (classCounter.size() == 0) {
                //For multi-pom, in the future, this should be called by the server once all the compilation tasks
                // were done
                //                scheduler.endAnalysis();  o lo hace el plugin o lo hace el Main, aquí no más
                started = false;
            }

        if (DEBUG)
            System.out.println("STARTED FOR " + arg0.getSourceFile() + "( " +
                    (arg0.getSourceFile() == null ? "" : arg0.getSourceFile().getName()) + " ) " + arg0.getKind());

    }


}
