package database.querys.cypherWrapper.cmu.pq;

import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Any;
import database.querys.cypherWrapper.Binop;
import database.querys.cypherWrapper.Case;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.CreateClause;
import database.querys.cypherWrapper.ExprImpl;
import database.querys.cypherWrapper.Expression;
import database.querys.cypherWrapper.ForEach;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.ReturnClause;
import database.querys.cypherWrapper.SetClause;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.WhereClause;
import database.querys.cypherWrapper.WithClause;
import database.querys.services.FieldServices;
import database.querys.services.InmutabilityServicesPQ;
import utils.dataTransferClasses.Pair;

public class OBJ50_V2_04 extends AbstractQuery {

	private static final Expression ATTR_PROPERTIES = new ExprImpl(" attr.isFinal AND attr.accessLevel='public' ");

	private static final Clause[] PUBLIC_TYPES_AND_PUBLIC_FINAL_FIELDS = new WhereClause(ATTR_PROPERTIES)
			.addToClauses(FieldServices.typesAndDeclaredFieldsAndTypes(
					new CompleteNode("declaringType", Pair.create("accessLevel", "public"))));

	// public OBJ50() {
	//
	// super(new Clause[] { PUBLIC_TYPES_AND_PUBLIC_FINAL_FIELDS[0],
	// PUBLIC_TYPES_AND_PUBLIC_FINAL_FIELDS[1],
	// new SimpleWithClause("attr", "declaringType", "typeDec"),
	// new MatchClause(InmutabilityServices.getTypesAndFieldsTransitive(new
	// NodeVar("typeDec"))),
	// WhereClause.getFilterOnNodeLabels("typeDec", NodeTypes.CLASS_DECLARATION,
	// NodeTypes.ENUM_DECLARATION),
	// new CreateClause(new CompleteNode("res", Pair.create("res", true))),
	// new WithClause(new String[] { "attr", "declaringType", "length", "res",
	// "nodesView", "nodes" },
	// new Expression[] { new ExprImpl("attr"), new ExprImpl("declaringType"),
	// new ExprImpl("LENGTH(p)"), new ExprImpl("res"),
	// new ExprImpl(
	// "extract(x IN nodes(p) | CASE WHEN x:ATTR_DEC THEN x.name ELSE
	// x.fullyQualifiedName END)"),
	// new ExprImpl(" nodes(p) ") }),
	// ListsServices.forAllEvensInList("length", 2, "res.res",
	// "NOT(nodes[i].accessLevel='public' AND nodes[i+1].accessLevel='public'
	// AND NOT nodes[i+1].isFinal AND NOT nodes[i+1].isStatic)"),
	// new SimpleWithClause("attr", "declaringType", "nodes", "res",
	// "nodesView"),
	// InmutabilityServices.getOptionalMutationsOfFields(),
	// new WhereClause(new ExprImpl("field IN nodes AND NOT
	// ass:INITIALIZATION")),
	// // new WithClause(new String[] { "attr", "declaringType",
	// // "nodes", "res" }, null, null, null,
	// // new ExprImpl("COUNT(mutationRel)=0 AND res.res")),
	// ReturnClause.fromStringsToReturnWithExprs("attr", "declaringType",
	// "nodesView",
	// "COUNT(ass), COLLECT([ass, LABELS(ass)]) , res.res"),
	// new OrderByClause("attr", "declaringType") });
	//
	// }
	// new Extract("NODES(p)", new Case("x:ATTR_DEC", "x.name",
	// "x.fullyQualifiedName"))),

	public OBJ50_V2_04(boolean isProgQuery) {
		super(isProgQuery);

	}

	// WTIH ATTR, DECLARING TYPE
	public static void main(String[] args) {
		System.out.println(new OBJ50_V2_04(true).queryToString());
	}

	@Override
	protected void initiate() {
		clauses = new Clause[] { PUBLIC_TYPES_AND_PUBLIC_FINAL_FIELDS[0], PUBLIC_TYPES_AND_PUBLIC_FINAL_FIELDS[1],
				new SimpleWithClause("attr", "declaringType", "typeDec"),
				new MatchClause(InmutabilityServicesPQ.getTypesSuperTypesAndFieldsTransitive(new NodeVar("typeDec"))),
				new WithClause(new String[] { "attr", "declaringType", "typeDec" },
						Pair.create("nodes", new ExprImpl("NODES(p)"))),
				new CreateClause(new CompleteNode("res", Pair.create("res", true))),
				new ForEach("i", "RANGE(1,SIZE(nodes),1)",
						new SetClause(new ExprImpl("res.res"),
								new Binop("AND", new ExprImpl("res.res"), new Case("nodes[i]:ATTR_DEC",
										InmutabilityServicesPQ.isInmutableField("nodes[i]", "nodes[i-1]"), "TRUE")))),
				new SimpleWithClause("attr", "declaringType", "typeDec", "nodes", "res"),
				// InmutabilityServices.getOptionalMutationsOfFields(),
				InmutabilityServicesPQ.getOptionalModificationsOfFields(),
				new WhereClause(new ExprImpl("field IN nodes AND NOT ass:INITIALIZATION")),
				new WithClause(new String[] { "attr", "declaringType", "res", "typeDec" },
						Pair.create("hasAnyMutationRel", new ExprImpl("COUNT(mutationRel)>0"))),
				new WithClause(new String[] { "attr", "declaringType", "typeDec, res" },
						Pair.create("isMutable", Any.collectAndAny(" NOT res.res OR hasAnyMutationRel", "x"))),
				new ClauseImpl("DELETE res"), new SimpleWithClause("attr", "declaringType", "typeDec", "isMutable"),
				new WhereClause("isMutable"), new ReturnClause(
						" DISTINCT 'Warning [CMU-OBJ50] Attribute '+ attr.name+' declared in '+declaringType.fullyQualifiedName+ ' is not actually final, only the reference. This is due to the type '+ typeDec.fullyQualifiedName+' is mutable ( the state of the attributes change in the program or may potentially be changed by a client).'") };
	}

}
