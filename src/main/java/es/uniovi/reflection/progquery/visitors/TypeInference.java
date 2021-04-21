package es.uniovi.reflection.progquery.visitors;

import java.util.List;

import javax.lang.model.element.Name;

import com.sun.source.util.TreeScanner;

import es.uniovi.reflection.progquery.database.relations.CFGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.PartialRelation;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;

public class TypeInference extends
TreeScanner<List<PartialRelation<CFGRelationTypes>>, Pair<Name, List<PartialRelation<CFGRelationTypes>>>> {

}
