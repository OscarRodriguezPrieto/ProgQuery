package database.querys.cypherWrapper.cmu.wiggle;

import database.nodes.NodeTypes;
import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.All;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.ExprImpl;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.UnwindClause;
import database.querys.cypherWrapper.WhereClause;
import database.querys.cypherWrapper.WithClause;
import database.querys.services.StatementServices;
import utils.dataTransferClasses.Pair;

public class OBJ54Long extends AbstractQuery {

	public OBJ54Long() {
		super(false);
	}

	@Override
	protected void initiate() {
		NodeVar assign = new NodeVar("assign");
		clauses = new Clause[] {
				new MatchClause(
						getPDGServices().getIdsAndVarDeclarations(Node.nodeForWiggle("id", NodeTypes.IDENTIFIER))),
				new WithClause(new String[] { "varDec" },
						Pair.create("identifications", new ExprImpl("COLLECT( DISTINCT id)")))

				//
				, new MatchClause( StatementServices.WIGGLE.getEnclosingClassFromDeclaration(new NodeVar("stat")),

						getAssignmentServices().getLeftPartAssignments(
								getAssignmentServices().getRightPartAssignments(
										Node.nodeForWiggle(NodeTypes.LITERAL, Pair.create("typeKind", "NULL"))),
								Node.nodeForWiggle("id", NodeTypes.IDENTIFIER)),
						getExpressionServices().getStatementFromExp(assign)),
				new WhereClause("id IN identifications ")
				.append(getCFGServices().getCFGSuccesorsOf(new NodeVar("stat"),
						" enclClass, varDec, identifications, "))
				,
//				new ReturnClause("DISTINCT varDec,stat, enclClass, identifications, succesors")
//				new SimpleWithClause("stat as assignStat, enclClass, varDec, identifications,  succesors")
		
				new UnwindClause("identifications", "id"),
				new MatchClause(true,
						getAssignmentServices().getLeftPartAssignments(new NodeVar("anyAssign"), new NodeVar("id"))),
				new SimpleWithClause("stat as assignStat, id, anyAssign, succesors, varDec, enclClass"),

//				new WhereClause("anyAssign IS NULL "),

				new MatchClause(getExpressionServices().getStatementFromExp(new NodeVar("id")))
//				, new UnwindClause("succesors", "succesor"),
				// new CreateClause(new MatchImpl(
				// "(assign)-[:REL_ONE]->( succesor),(assign)-[:REL_TWO]->(
				// stat),
				// (assign)-[:REL_THREE]->(varDec),(assign)-[:REL_FOUR]->(enclClass)")),
				,new ClauseImpl(
						// "MATCH (assign)-[:REL_ONE]->(
						// succesor),(assign)-[:REL_TWO]->( stat),
						// (assign)-[:REL_THREE]->(varDec),(assign)-[:REL_FOUR]->(enclClass)
						// \n"+
						" WITH  assignStat,varDec,enclClass, FILTER( x IN COLLECT(CASE WHEN anyAssign IS NULL THEN stat ELSE NULL END) WHERE NOT x IS NULL) as useStats, succesors"
						+ "\n WHERE  "
								+ new All("useStats", "NOT x IN succesors").expToString() + " RETURN 	"
								+ "'Warning [CMU-OBJ54] You must not try to help garbage collector setting references to null when they are no longer used. To make your code clearer, just delete the assignment in line ' + assignStat.lineNumber + ' of the variable ' +varDec.name+ ' declared in line '+varDec.lineNumber+',class '+enclClass.fullyQualifiedName+'.'"
						) 
		
		};
						
	}

	public static void main(String[] args) {
		System.out.println(new OBJ54Long().queryToString());
	}

}
