package test;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTaskImpl;

import ast.ASTAuxiliarStorage;
import database.DatabaseFachade;
import database.nodes.NodeTypes;
import database.querys.MainQuery;
import database.relations.RelationTypes;
import test.utils.CompilerUtils;
import utils.JavacInfo;
import utils.Pair;
import visitors.ASTTypesVisitor;
import visitors.PDGVisitor;

public abstract class GDBAPIBasedTest {
	protected GraphDatabaseService graphDb;

	private Transaction transaction;

	public abstract String getFileName();

	@Before
	public void prepareTestDatabase() throws Exception {
		graphDb = DatabaseFachade.getDB();
		JavacTaskImpl task = CompilerUtils.getTask(getFileName());

		List<? extends CompilationUnitTree> parse = (List<? extends CompilationUnitTree>) task.parse();
		task.analyze();

		CompilationUnitTree u = parse.get(0);
		Tree t = u.getTypeDecls().get(0);
		JavacInfo.setJavacInfo(new JavacInfo(u, task));
		DatabaseFachade.setDB(graphDb);
		transaction = graphDb.beginTx();
		graphDb.execute(MainQuery.DELETE_ALL);
		ASTAuxiliarStorage ast = new ASTAuxiliarStorage();
		PDGVisitor pdgUtils;
		new ASTTypesVisitor(t, true, pdgUtils = new PDGVisitor(), ast).scan(u, Pair.createPair(
				DatabaseFachade.createSkeletonNode(u, NodeTypes.COMPILATION_UNIT), RelationTypes.CU_PACKAGE_DEC));
		ast.doDynamicMethodCallAnalysis();
		ast.doInterproceduralPDGAnalysis(pdgUtils.getMethodsMutateThisAndParams(), pdgUtils.getParamsMutatedInMethods(),
				pdgUtils.getParamsMayMutateInMethods(), pdgUtils.getThisRefsOfMethods());
	}

	@After
	public void destroyTestDatabase() {
		// Result result = graphDb.execute(
		// "start n=node(*) MATCH m-[r:CFG_ENTRY | CFG_END_OF |
		// CFG_NEXT_CONDITION | CFG_NEXT_STATEMENT | CFG_NEXT_COND_IF_TRUE |
		// CFG_NEXT_STATEMENT_IF_TRUE | CFG_NEXT_COND_IF_FALSE |
		// CFG_NEXT_STATEMENT_IF_FALSE]->n RETURN m,r,n");
		// Result result = graphDb.execute(MainQuery.ALL_NODES);
		// System.out.println(result.resultAsString());
		try {
			transaction.success();
		} finally {
			transaction.close();
		}
		graphDb.shutdown();
	}

}
