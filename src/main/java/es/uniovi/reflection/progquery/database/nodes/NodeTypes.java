package es.uniovi.reflection.progquery.database.nodes;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Label;

public enum NodeTypes implements Label {

	ANNOTATION(NodeCategory.AST_NODE), ANNOTATED_TYPE(NodeCategory.AST_TYPE), ARRAY_ACCESS(NodeCategory.LVALUE,
			NodeCategory.EXPRESSION), ARRAY_TYPE, ASSERT_STATEMENT(NodeCategory.STATEMENT), ASSIGNMENT(
					NodeCategory.EXPRESSION), ATTR_DEF(NodeCategory.VARIABLE_DEF,
							NodeCategory.DEFINITION), BINARY_OPERATION(NodeCategory.EXPRESSION), BLOCK(
									NodeCategory.STATEMENT), BREAK_STATEMENT(NodeCategory.STATEMENT), CASE_STATEMENT(

											NodeCategory.STATEMENT), CATCH_BLOCK(

													NodeCategory.STATEMENT), CLASS_DEF(NodeCategory.TYPE_DEFINITION,
															NodeCategory.DEFINITION,
															NodeCategory.TYPE_NODE), COMPILATION_UNIT(
																	NodeCategory.AST_NODE), COMPOUND_ASSIGNMENT(

																			NodeCategory.EXPRESSION), CONDITIONAL_EXPRESSION(

																					NodeCategory.EXPRESSION), CONSTRUCTOR_DEF(

																							NodeCategory.CALLABLE_DEF,
																							NodeCategory.DEFINITION), CONTINUE_STATEMENT(

																									NodeCategory.STATEMENT), DO_WHILE_LOOP(

																											NodeCategory.STATEMENT), ERROR_TYPE(

																													NodeCategory.TYPE_NODE), EMPTY_STATEMENT(

																															NodeCategory.STATEMENT), FOR_EACH_LOOP(

																																	NodeCategory.STATEMENT), ENUM_DEF(

																																			NodeCategory.TYPE_DEFINITION,
																																			NodeCategory.DEFINITION,
																																			NodeCategory.TYPE_NODE), ENUM_ELEMENT(
																																					NodeCategory.AST_NODE), ERRONEOUS_NODE, CALLABLE_TYPE(

																																							NodeCategory.TYPE_NODE), EXPRESSION_STATEMENT(

																																									NodeCategory.STATEMENT), FINALLY_BLOCK(

																																											NodeCategory.STATEMENT), FOR_LOOP(

																																													NodeCategory.STATEMENT), IDENTIFIER(
																																															NodeCategory.AST_NODE), IF_STATEMENT(

																																																	NodeCategory.STATEMENT), IMPORT(
																																																			NodeCategory.AST_NODE), INSTANCE_OF(

																																																					NodeCategory.EXPRESSION), INTERFACE_DEF(
																																																							NodeCategory.TYPE_DEFINITION,
																																																							NodeCategory.DEFINITION,
																																																							NodeCategory.TYPE_NODE), INTERSECTION_TYPE, LABELED_STATEMENT(

																																																											NodeCategory.STATEMENT), LAMBDA_EXPRESSION(

																																																													NodeCategory.EXPRESSION), LITERAL(

																																																															NodeCategory.EXPRESSION), MEMBER_SELECTION(
																																																																	NodeCategory.AST_NODE), MEMBER_REFERENCE(

																																																																			NodeCategory.EXPRESSION), METHOD_DEF(

																																																																					NodeCategory.CALLABLE_DEF,
																																																																					NodeCategory.DEFINITION), METHOD_INVOCATION(

																																																																							NodeCategory.EXPRESSION,
																																																																							NodeCategory.CALL), NEW_ARRAY(

																																																																									NodeCategory.EXPRESSION), NEW_INSTANCE(

																																																																											NodeCategory.EXPRESSION,
																																																																											NodeCategory.CALL), NULL_TYPE(

																																																																													NodeCategory.TYPE_NODE), PACKAGE(
																																																																															NodeCategory.PACKAGE_NODE), PACKAGE_TYPE(

																																																																																	NodeCategory.TYPE_NODE), PARAMETER_DEF(

																																																																																			NodeCategory.LOCAL_DEF,
																																																																																			NodeCategory.VARIABLE_DEF,
																																																																																			NodeCategory.DEFINITION), GENERIC_TYPE, PRIMITIVE_TYPE, RETURN_STATEMENT(

																																																																																					NodeCategory.STATEMENT), SWITCH_STATEMENT(

																																																																																							NodeCategory.STATEMENT), SYNCHRONIZED_BLOCK(

																																																																																									NodeCategory.STATEMENT), THIS_REF(
																																																																																											NodeCategory.PDG_NODE), THROW_STATEMENT(

																																																																																													NodeCategory.STATEMENT), TRY_STATEMENT(

																																																																																															NodeCategory.STATEMENT), TYPE_CAST(

																																																																																																	NodeCategory.EXPRESSION), TYPE_PARAM, UNARY_OPERATION(

																																																																																																			NodeCategory.EXPRESSION), UNION_TYPE

																																																																																																					, LOCAL_VAR_DEF(

																																																																																																							NodeCategory.LOCAL_DEF,
																																																																																																							NodeCategory.VARIABLE_DEF,
																																																																																																							NodeCategory.DEFINITION,
																																																																																																							NodeCategory.STATEMENT), WHILE_LOOP(

																																																																																																									NodeCategory.STATEMENT), WILDCARD_TYPE, VOID_TYPE(

																																																																																																											NodeCategory.TYPE_NODE), TYPE_VARIABLE(
																																																																																																													NodeCategory.TYPE_NODE), UNKNOWN_TYPE(

																																																																																																															NodeCategory.TYPE_NODE)

	, INITIALIZATION(NodeCategory.PDG_NODE), CFG_NORMAL_END(NodeCategory.CFG_NODE), CFG_ENTRY(NodeCategory.CFG_NODE), CFG_EXCEPTIONAL_END(NodeCategory.CFG_NODE), CFG_LAST_STATEMENT_IN_FINALLY(NodeCategory.CFG_NODE), PROGRAM(NodeCategory.PACKAGE_NODE);

	private NodeTypes(NodeCategory... hypernyms) {
		this();
		for (NodeCategory cat : hypernyms) {
			if (cat == NodeCategory.STATEMENT || cat == NodeCategory.EXPRESSION || cat == NodeCategory.AST_TYPE
					|| cat == NodeCategory.CALLABLE_DEF || cat == NodeCategory.VARIABLE_DEF)
				if (!this.hypernyms.contains(NodeCategory.AST_NODE))
					this.hypernyms.add(NodeCategory.AST_NODE);
			this.hypernyms.add(cat);
		}
	}

	private NodeTypes() {
		hypernyms = new ArrayList<NodeCategory>();
		hypernyms.add(NodeCategory.PQ_NODE);
	}

	public final List<NodeCategory> hypernyms;
	// public static final NodeCategory[] AST_TYPE_HYPER = {
	// NodeCategory.AST_NODE, NodeCategory.AST_TYPE };

}
