package typeInfo;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import com.sun.tools.javac.code.Symbol;

import cache.DefinitionCache;
import cache.NotDuplicatingArcsDefCache;
import database.DatabaseFachade;
import database.nodes.NodeTypes;
import database.relations.CDGRelationTypes;
import node_wrappers.NodeWrapper;
import utils.dataTransferClasses.Pair;

public class PackageInfo {
	public static NodeWrapper currentProgram;

	public static void createCurrentProgram(String id) {
		currentProgram = DatabaseFachade.CURRENT_DB_FACHADE.createNodeWithoutExplicitTree(NodeTypes.PROGRAM);
		currentProgram.setProperty("ID", id);
		// SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at'
		// HH:mm:ss z");
		// java.time.LocalDateTime currentDate=LocalDateTime.now();
		// DateValue currentDate = new DateValue(LocalDate.now());
		// Date currentDate = Date.from(Instant.now());
		// ZonedDateTime currentDate = ZonedDateTime.now();
		currentProgram.setProperty("timestamp", ZonedDateTime.now().toString());
		// System.out.println(formatter.format(date));

	}

	public static PackageInfo PACKAGE_INFO = new PackageInfo();
	// private final Map<Symbol, Node> packageSet = new HashMap<>();
	private final DefinitionCache<Symbol> packageCache = new NotDuplicatingArcsDefCache<>();

	private final Set<Pair<Symbol, Symbol>> dependenciesSet = new HashSet<>();
	public Symbol currentPackage;

	private NodeWrapper addPackage(Symbol s, boolean isDeclared) {
		NodeWrapper packageNode = DatabaseFachade.CURRENT_DB_FACHADE.createNodeWithoutExplicitTree(NodeTypes.PACKAGE);
		// packageSet.put(s, packageNode);
		if (isDeclared) {
			packageCache.putDefinition(s, packageNode);
			currentProgram.createRelationshipTo(packageNode, CDGRelationTypes.PROGRAM_DECLARES_PACKAGE);
		} else
			packageCache.put(s, packageNode);
		packageNode.setProperty("name", s.toString());
		packageNode.setProperty("isDeclared", isDeclared);
		return packageNode;
	}

	private void addDependency(Symbol dependent, Symbol dependency) {
		dependenciesSet.add(Pair.create(dependent, dependency));
	}

	public NodeWrapper getPackageNode(Symbol packageSymbol) {
		return packageCache.get(packageSymbol);
	}

	public NodeWrapper putDeclaredPackage(Symbol packageSymbol) {

		if (packageCache.containsDef(packageSymbol))
			return getPackageNode(packageSymbol);
		else {
			// si esta en la cache no declarada
			NodeWrapper packageNode = getPackageNode(packageSymbol);
			if (packageNode != null) {
				packageNode.setProperty("isDeclared", true);
				currentProgram.createRelationshipTo(packageNode, CDGRelationTypes.PROGRAM_DECLARES_PACKAGE);
			} else
				packageNode = addPackage(packageSymbol, true);
			packageCache.putDefinition(packageSymbol, packageNode);
			return packageNode;
		}
	}

	private boolean hasDependency(Symbol dependent, Symbol dependency) {
		return dependenciesSet.contains(Pair.create(dependent, dependency));
	}

	public void handleNewDependency(Symbol dependency) {
		handleNewDependency(currentPackage, dependency);
	}

	public void handleNewDependency(Symbol dependent, Symbol dependency) {
		if (!dependent.equals(dependency) && !hasDependency(dependent, dependency)) {
			addDependency(dependent, dependency);
			NodeWrapper dependentNode = getPackageNode(dependent), denpendencyNode = getPackageNode(dependency);
			if (dependentNode == null)
				dependentNode = addPackage(dependent, false);

			if (denpendencyNode == null)
				denpendencyNode = addPackage(dependency, false);
			dependenciesSet.add(Pair.create(dependent, dependency));
		}
	}

	public void createStoredPackageDeps() {
		for (Pair<Symbol, Symbol> packageDep : dependenciesSet) {
			NodeWrapper dependencyPack = packageCache.get(packageDep.getSecond());
			// System.out.println("CREATING REL:\n" + packageDep.getFirst());
			// System.out.println(packageDep.getSecond());
			// System.out.println((Boolean)
			// dependencyPack.getProperty("isDeclared") ?
			// CDGRelationTypes.DEPENDS_ON_PACKAGE
			// : CDGRelationTypes.DEPENDS_ON_NON_DECLARED_PACKAGE);
			// packageCache.get(packageDep.getFirst()).createRelationshipTo(dependencyPack,
			// (Boolean) dependencyPack.getProperty("isDeclared") ?
			// CDGRelationTypes.DEPENDS_ON_PACKAGE
			// : CDGRelationTypes.DEPENDS_ON_NON_DECLARED_PACKAGE);
			if ((Boolean) dependencyPack.getProperty("isDeclared"))
				packageCache.get(packageDep.getFirst()).createRelationshipTo(dependencyPack,
						CDGRelationTypes.DEPENDS_ON_PACKAGE);
			else
				packageCache.get(packageDep.getFirst()).createRelationshipTo(dependencyPack,
						CDGRelationTypes.DEPENDS_ON_NON_DECLARED_PACKAGE);
				
		}
	}
}
