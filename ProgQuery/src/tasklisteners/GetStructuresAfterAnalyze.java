package tasklisteners;

import java.util.HashMap;
import java.util.Map;

import javax.tools.JavaFileObject;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

import ast.ASTAuxiliarStorage;
import cache.DefinitionCache;
import database.DatabaseFachade;
import database.nodes.NodeTypes;
import database.relations.CDGRelationTypes;
import database.relations.PartialRelation;
import database.relations.RelationTypes;
import node_wrappers.NodeWrapper;
import typeInfo.PackageInfo;
import utils.JavacInfo;
import utils.dataTransferClasses.Pair;
import visitors.ASTTypesVisitor;
import visitors.PDGProcessing;

public class GetStructuresAfterAnalyze implements TaskListener {
	private static final boolean DEBUG = false;
	private final JavacTask task;
	private Map<JavaFileObject, Integer> classCounter = new HashMap<JavaFileObject, Integer>();
	// private Set<CompilationUnitTree> unitsInTheSameFile = new
	// HashSet<CompilationUnitTree>();
	private boolean started = false;
	private boolean firstClass = true;

	private int counter = 0;

	// private Transaction transaction;
	private Pair<PartialRelation<RelationTypes>, Object> argument;
	private CompilationUnitTree cu;
	private PDGProcessing pdgUtils = new PDGProcessing();
	private ASTAuxiliarStorage ast = new ASTAuxiliarStorage();
	// private final GraphDatabaseService graphDb;

	public GetStructuresAfterAnalyze(JavacTask task, String programID) {
		this.task = task;
		// this.graphDb = graphDb;
		DatabaseFachade.CURRENT_INSERTION_STRATEGY.startAnalysis();
		PackageInfo.createCurrentProgram(programID);

	}

	@Override
	public void finished(TaskEvent arg0) {
		if (DEBUG)
			System.out.println("FINISHING " + arg0.getKind());
		CompilationUnitTree cuTree = arg0.getCompilationUnit();
		if (arg0.getKind() == Kind.PARSE) {
			// if (DEBUG)
			// System.out.println("FIle " + cuTree.getSourceFile().getName() + "
			// , " + cuTree.hashCode() + " , "
			// + cuTree.getSourceFile().hashCode());
			// System.out.println(
			// "TOTAL DECS FOR " + cuTree.getSourceFile().getName() + " : " +
			// cuTree.getTypeDecls().size());
			classCounter.put(cuTree.getSourceFile(), cuTree.getTypeDecls().size());
			// System.out.println("PUTTING FOR " +
			// cuTree.getSourceFile().getName() + "\t"
			// + classCounter.get(cuTree.getSourceFile()));
		} else if (arg0.getKind() == Kind.ANALYZE) {

			started = true;
			classCounter.put(cuTree.getSourceFile(), classCounter.get(cuTree.getSourceFile()) - 1);
			// System.out.println("DELETING FOR " +
			// cuTree.getSourceFile().getName() + ":\t"
			// + classCounter.get(cuTree.getSourceFile()));
			if (firstClass) {
				firstClass = false;
				// Node packageNode = DatabaseFachade.createSkeletonNode(
				// NodeTypes.PACKAGE_DEC);
				//// packageNode.setProperty("name",
				// ((JCCompilationUnit)cuTree).);
				// System.out.println(((JCCompilationUnit)
				// cuTree).getPackageName());
				// if (DEBUG)
				// System.out.println("TYPE_DECS_IN_CU:\t" +
				// cuTree.getTypeDecls().size());
//				System.out.println("BEFORE SCAN");
				if (cuTree.getTypeDecls().size() > 0)
					firstScan(cuTree, (ClassTree) cuTree.getTypeDecls().get(counter++));
				else
					firstScanIfNoTypeDecls(cuTree);
//				System.out.println("AFTER SCAN");
			} else if (cuTree.getTypeDecls().size() > 0)
				scan((ClassTree) cuTree.getTypeDecls().get(counter++), false);

			if (classCounter.get(cuTree.getSourceFile()) <= 0) {
				// END OF THE ANALYSIS OF ALL TYPEDECS IN THE COMPILATION UNIT
				classCounter.remove(cuTree.getSourceFile());
				firstClass = true;
				counter = 0;
				// System.out.println("AFTER ANALYZE");
				// System.out.println(ast.mm + "\n" + ast.b + "\n" + ast.s1);
				// transaction.success();
				// transaction.close();

			}

		}
		if (DEBUG)
			System.out.println("FINISHED " + arg0.getKind());
	}

	private void firstScanIfNoTypeDecls(CompilationUnitTree u) {
//		System.out.println("BEFORE SETTING JAVAC INFO");
		JavacInfo.setJavacInfo(new JavacInfo(u, task));
//		System.out.println("AFTER SETTING JAVAC INFO");
		String fileName = u.getSourceFile().toUri().toString();
		// transaction = DatabaseFachade.beginTx();

		NodeWrapper compilationUnitNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(u,
				NodeTypes.COMPILATION_UNIT);

		addPackageInfo(((JCCompilationUnit) u).packge, compilationUnitNode);
		compilationUnitNode.setProperty("fileName", fileName);

		argument = Pair.createPair(compilationUnitNode, null);
		cu = u;

	}

	private NodeWrapper addPackageInfo(Symbol currentPackage, NodeWrapper compilationUnitNode) {

		PackageInfo.PACKAGE_INFO.currentPackage = currentPackage;
		NodeWrapper packageNode = PackageInfo.PACKAGE_INFO.putDeclaredPackage(currentPackage);
		packageNode.createRelationshipTo(compilationUnitNode, CDGRelationTypes.PACKAGE_HAS_COMPILATION_UNIT);
		// packageNode.setProperty("isDeclared", true);
		return packageNode;
	}

	private void firstScan(CompilationUnitTree u, ClassTree typeDeclaration) {
//		System.out.println("BEFORE SETTING JAVAC INFOf");
		JavacInfo.setJavacInfo(new JavacInfo(u, task));
//		System.out.println("AFTER SETTING JAVAC INFOf");

		String fileName = u.getSourceFile().getName();
		// transaction = DatabaseFachade.beginTx();

		// InsertionStrategy.CURRENT_INSERTION_STRATEGY.startAnalysis();

		NodeWrapper compilationUnitNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(u,
				NodeTypes.COMPILATION_UNIT);
		addPackageInfo(((JCCompilationUnit) u).packge, compilationUnitNode);
		// System.out.println(fileName);
		compilationUnitNode.setProperty("fileName", fileName);

		argument = Pair.createPair(compilationUnitNode, null);
		cu = u;
//		System.out.println("BEFORE SCAN TYPEDEC");
		scan(typeDeclaration, true);
//		System.out.println("AFTER SCAN TYPEDEC");

	}

	private void scan(ClassTree typeDeclaration, boolean first) {
		// if (DEBUG) {
		// System.err.println("-*-*-*-*-*-*-* NEW TYPE DECLARATION AND
		// VISITOR-*-*-*-*-*-*-*");
		// System.err.println(cu.getSourceFile().getName());
		// System.out.println("Final State:\n");
		//
		// // System.out.println(typeDeclaration);
		// }
		DefinitionCache.ast = ast;

		new ASTTypesVisitor(typeDeclaration, first, pdgUtils, ast, argument.getFirst().getStartingNode()).scan(cu,
				argument);
	}

	@Override
	public void started(TaskEvent arg0) {

		if (DEBUG)
			System.out.println("STARTING FOR " + arg0.getSourceFile() + " " + arg0.getKind());
		if (arg0.getKind() == Kind.GENERATE && started)
			// System.out.println(classCounter.size());
			if (classCounter.size() == 0) {
//			System.out.println("BEFORE 2nd phase ");
			pdgUtils.createNotDeclaredAttrRels(ast);
			createStoredPackageDeps();
			dynamicMethodCallAnalysis();
			interproceduralPDGAnalysis();
			initializationAnalysis();

			shutdownDatabase();
			started = false;
			}

		// if (DEBUG)
		// System.out.println("STARTED FOR " + arg0.getSourceFile() + " " +
		// arg0.getKind());

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
		if (DEBUG)
			System.out.println("SHUTDOWN THE DATABASE");
		// graphDb.shutdown();
		// AQUí IRÍA EL CÓDIGO DE INSERCIÓN AL SERVER
		DatabaseFachade.CURRENT_INSERTION_STRATEGY.endAnalysis();
		if (DEBUG)
			System.out.println("SHUTDOWN THE DATABASE ENDED");

	}

}
