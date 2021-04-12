package visitors;

import java.util.List;

import javax.lang.model.element.Name;

import com.sun.source.util.TreeScanner;

import database.relations.CFGRelationTypes;
import database.relations.PartialRelation;
import utils.dataTransferClasses.Pair;

public class TypeInference extends
TreeScanner<List<PartialRelation<CFGRelationTypes>>, Pair<Name, List<PartialRelation<CFGRelationTypes>>>> {

}
