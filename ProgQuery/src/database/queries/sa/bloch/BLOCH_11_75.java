package database.queries.sa.bloch;

import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.ExprImpl;
import database.querys.cypherWrapper.Expression;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchImpl;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.RelationshipImpl;
import database.querys.cypherWrapper.ReturnClause;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.WhereClause;
import database.querys.services.FieldServices;
import database.relations.PDGRelationTypes;
import utils.dataTransferClasses.Pair;

public class BLOCH_11_75 extends AbstractQuery {

	public BLOCH_11_75() {
		super(true);

	}

	// WTIH ATTR, DECLARING TYPE
	public static void main(String[] args) {
		System.out.println(new BLOCH_11_75().queryToString());
	}

	@Override
	protected void initiate() {
		clauses = new Clause[] {

new ClauseImpl("MATCH (cu)-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]->(typeDec)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]->( {fullyQualifiedName:'java.io.Serializable'})"
		+ "\n OPTIONAL MATCH (typeDec)-[:DECLARES_METHOD]->(method)-[:CALLABLE_HAS_THROWS]->(throwClause) "
		+ "\n WITH cu, typeDec, method, COLLECT(throwClause.actualType) as throws "
		+ "\n WHERE  NOT( method.fullyQualifiedName ENDS WITH ':readObject(java.io.ObjectInputStream)void' AND method.accessLevel='private' AND SIZE(throws)=2 AND 'java.io.IOException' IN throws AND 'java.lang.ClassNotFoundException' IN throws AND NOT method.isStatic AND NOT method.isFinal AND NOT method.isSynchronized AND NOT method.isStrictfp AND NOT method.isNative "
		+ " OR method.fullyQualifiedName ENDS WITH ':readResolve()java.lang.Object' AND SIZE(throws)=1 AND throws[0]='java.io.ObjectStreamException')"
		+ ""
		+ "\n RETURN DISTINCT '[BLOCH-10.75;'+ cu.fileName +';type_definition;examples.test.rule_10_75.NC1;'+ typeDec.lineNumber +'; You must implement the standard readObject method (or, at least, the readResolve method) for all your classes implementing java.io.Serializable.]'")
};
		}

}
