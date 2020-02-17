package database.querys.cypherWrapper.cmu.wiggle;

import database.querys.cypherWrapper.All;
import database.querys.eval.Query;

public class OBJ54Part_2 implements Query {

	@Override
	public String queryToString() {
		return "MATCH (assign)-[:REL_ONE]->( succesor),(assign)-[:REL_TWO]->( stat), (assign)-[:REL_THREE]->(varDec),(assign)-[:REL_FOUR]->(enclClass) \n"
				+ " WITH  assign, COLLECT(stat) as useStats, COLLECT(succesor) as succesors\n WHERE  "
				+ new All("useStats", "NOT x IN succesors").expToString() + " RETURN 	"
				+ "'Warning [CMU-OBJ54] You must not try to help garbage collector setting references to null when they are no longer used. To make your code clearer, just delete the assignment in line ' + assign.lineNumber + ' of the variable ' +varDec.name+ ' declared in class '+enclClass.fullyQualifiedName+'.'";
	}

}
