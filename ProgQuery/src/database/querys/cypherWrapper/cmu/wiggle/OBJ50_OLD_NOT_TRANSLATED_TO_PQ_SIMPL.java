package database.querys.cypherWrapper.cmu.wiggle;

import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Binop;
import database.querys.cypherWrapper.Case;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.CreateClause;
import database.querys.cypherWrapper.EdgeDirection;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.ExprImpl;
import database.querys.cypherWrapper.Expression;
import database.querys.cypherWrapper.Extract;
import database.querys.cypherWrapper.ForEach;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.RelationshipImpl;
import database.querys.cypherWrapper.SetClause;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.UnwindClause;
import database.querys.cypherWrapper.WhereClause;
import database.querys.services.FieldServicesWiggle;
import database.querys.services.InmutabilityServicesWiggle;
import database.relations.RelationTypesWiggle;
import utils.dataTransferClasses.Pair;

public class OBJ50_OLD_NOT_TRANSLATED_TO_PQ_SIMPL extends AbstractQuery {

	/*
	 * MATCH
	 * (declaringType{accessLevel:'public'})-[:DECLARES_FIELD]->(attr{typeKind:'
	 * DECLARED'})-[:ITS_TYPE_IS]->(typeDec) WHERE attr.isFinal AND
	 * attr.accessLevel='public' WITH attr, declaringType, typeDec
	 * 
	 * MATCH
	 * p=(typeDec)-[:DECLARES_FIELD|ITS_TYPE_IS|IS_SUBTYPE_EXTENDS*]->(next{
	 * typeKind:'DECLARED'}) WITH attr, declaringType, typeDec, NODES(p) as
	 * nodes
	 * 
	 * CREATE (res{res:true}) FOREACH (i IN RANGE(1,SIZE(nodes),1) | SET res.res
	 * = res.res AND (CASE WHEN nodes[i]:ATTR_DEC THEN
	 * NOT(nodes[i-1].accessLevel='public' AND NOT nodes[i].isFinal AND NOT
	 * nodes[i].isStatic AND ( nodes[i].accessLevel='public' OR
	 * (nodes[i].accessLevel='protected' AND NOT nodes[i-1].isFinal) )) ELSE
	 * TRUE END)) WITH attr, declaringType, typeDec, nodes, res ------> TO DO
	 * 
	 * 
	 * 
	 * OPTIONAL MATCH
	 * (field)-[mutationRel:STATE_MODIFIED_BY|STATE_MAY_BE_MODIFIED|MODIFIED_BY]
	 * ->(ass) WHERE field IN nodes AND NOT ass:INITIALIZATION WITH attr,
	 * declaringType, res, typeDec, COUNT(mutationRel)>0 as hasAnyMutationRel
	 * 
	 * WITH attr, declaringType, typeDec, ANY( x IN COLLECT( NOT res.res OR
	 * hasAnyMutationRel) WHERE x ) as isMutable WHERE isMutable RETURN 'Warning
	 * [CMU-OBJ50] Attribute '+ attr.name+' declared in
	 * '+declaringType.fullyQualifiedName+ ' is not actually final, only the
	 * reference. This is due to the type '+ typeDec.fullyQualifiedName+' is
	 * mutable ( the state of the attributes change in the program or may
	 * potentially be changed by a client).'
	 * 
	 * 
	 */
	private static final Expression ATTR_PROPERTIES = new ExprImpl(
			"declaringTypeModfiers.flags CONTAINS 'public' AND attrModifiers.flags CONTAINS 'public' AND  attrModifiers.flags CONTAINS 'final' AND attrModifiers.position>-1");
	private static final Clause[] PUBLIC_TYPES_AND_PUBLIC_FINAL_FIELDS = new WhereClause(ATTR_PROPERTIES)
			.addToClauses(FieldServicesWiggle.typesAndDeclaredFieldsPlusModifiersAndTypes(
					new RelationshipImpl(new NodeVar("declaringTypeModfiers"), new CompleteNode("declaringType"),
							new EdgeImpl(EdgeDirection.OUTGOING, RelationTypesWiggle.HAS_CLASS_MODIFIERS))));

	public OBJ50_OLD_NOT_TRANSLATED_TO_PQ_SIMPL() {
		super(false);
	}

	private static final String CREATE_ITS_TYPE_IS_RELS = "MATCH (n),(m) WHERE EXISTS(n.actualType) AND n.actualType=m.fullyQualifiedName CREATE (n)-[:ITS_TYPE_IS{created:TRUE}]->(m) WITH  'L' as l ";
	private static final String CREATE_ALL_SUBTYPE_RELS = "MATCH (n)-[:HAS_CLASS_EXTENDS]->(m)-[:ITS_TYPE_IS]->(t) MERGE (n)-[r:IS_SUBTYPE_EXTENDS]->(t) ON CREATE SET r.created=TRUE WITH  l ";

	@Override
	protected void initiate() {
		// TO_ DO ADD DELETE RELS
		clauses = new Clause[] { new ClauseImpl(CREATE_ITS_TYPE_IS_RELS), new ClauseImpl(CREATE_ALL_SUBTYPE_RELS),
				PUBLIC_TYPES_AND_PUBLIC_FINAL_FIELDS[0], PUBLIC_TYPES_AND_PUBLIC_FINAL_FIELDS[1],
				new SimpleWithClause("attr", "declaringType", "typeDec"),
				new MatchClause(
						InmutabilityServicesWiggle.getTypesSuperTypesAndFieldsTransitive(new NodeVar("typeDec"))),
				new UnwindClause("NODES(p)", "nodeInP"),
				new MatchClause(true,
						new RelationshipImpl(new NodeVar("nodeInP"),
								new EdgeImpl(EdgeDirection.OUTGOING, RelationTypesWiggle.DECLARES_FIELD)
										.setName("declaredFieldRel"))),
				new MatchClause(true,
						new RelationshipImpl(new NodeVar("nodeInP"), new NodeVar("modifiers"),
								new EdgeImpl(RelationTypesWiggle.HAS_VARIABLEDECL_MODIFIERS,
										RelationTypesWiggle.HAS_CLASS_MODIFIERS))),
				new SimpleWithClause(
						"attr, declaringType, typeDec,p, COLLECT([nodeInP, NOT declaredFieldRel IS NULL, modifiers]) as nodes"),

				new CreateClause(new CompleteNode("res:RES", Pair.create("res", true))),
				new ForEach("i", "RANGE(1,SIZE(nodes),1)",
						new SetClause(new ExprImpl("res.res"),
								new Binop("AND", new ExprImpl("res.res"), new Case("nodes[i][1]",
										InmutabilityServicesWiggle.isInmutableField("nodes[i][2]", "nodes[i-1][2]"),
										"TRUE")))),

				new SimpleWithClause("attr, declaringType, typeDec, " + new Extract("nodes", "x[0]").expToString()
						+ " as nodes, res"),
				getPDGServices().getModificationsOnFields("nodes", "attr, declaringType, typeDec, res, nodes"),
				new SimpleWithClause(
						"attr, declaringType, typeDec, res, nodes, assignsMods+COLLECT(DISTINCT assignment) as assignsMods"),
				new SimpleWithClause(
						"attr, declaringType, typeDec, res, nodes, COUNT(assignsMods)>0 as hasAnyMutationRel"),
				new SimpleWithClause(
						"attr, declaringType, typeDec, ANY( x IN COLLECT( NOT res.res OR hasAnyMutationRel) WHERE x ) as isMutable, res"),
				new ClauseImpl("DELETE res "), new SimpleWithClause("attr, declaringType, typeDec, isMutable"),
				new WhereClause("isMutable"),
				new SimpleWithClause(
						" DISTINCT 'Warning [CMU-OBJ50] Attribute '+ attr.name+' declared in '+declaringType.fullyQualifiedName+ ' is not actually final, only the reference. This is due to the type '+ typeDec.fullyQualifiedName+' is mutable ( the state of the attributes change in the program or may potentially be changed by a client).' as warning "),
				new ClauseImpl(
						" WITH COLLECT(warning) as wList MATCH ()-[r{created:TRUE}]->() DELETE r WITH DISTINCT wList RETURN wList")

		};
	}

	public static void main(String[] args) {
System.out.println(new OBJ50_OLD_NOT_TRANSLATED_TO_PQ_SIMPL().queryToString());
	}
}
