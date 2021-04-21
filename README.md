# ProgQuery
ProgQuery is a system to extract syntactic and semantic information from source code programs and store it in a Neo4j graph es.uniovi.reflection.progquery.database for posterior analysis. 
For each program or compilation unit, ProgQuery extracts the following graph structures:
- Abstract Syntax Tree
- Call Graph
- Type Graph
- Control Flow Graph
- Program Dependency Graph
- Class Dependency Graph
- Package Graph
# Build Instructions
- JDK8 or higher

# How to use
Currently, ProgQuery is avaliable as a plugin for the javac compiler.  
In order to use it to analyse programs during the compilation process:
- Download the content of the _binaries_ folder: _ProgQuery.jar_ and the jars placed in _lib_.
- Run this command to compile and analyse Java classes   
`javac -cp ProgQuery.jar;libs;<other_source_or_binary_files_necessary_for_compilation> -Xplugin:"ProgQueryPlugin projectName mode optArg1 optArg2" <directory>\<YourClass>.java`  
Where `projectName` is the name for the project to be stored in the DB; `mode` is just a letter specifying server ('S') or embedded ('L') mode,(embedded by default). `optArg1` may be used to specify the DB directory (only in embedded mode and `./neo4j/data/ProgQuery.db/` by default), to specify the number of operations per transaction (only in server mode and 80.000 by default) or to specify the connection chain as `user;pass;address:port` (only in server mode, iff opeerations per transaction is omitted).`optArg2` is used to specify the connection chain when opeartions per transaction is specified.
If during the compilation of certain class, it is necessary to compile another classes, all these compiled classes are going to be analysed and included in the es.uniovi.reflection.progquery.database.
  On the other hand, binary files (_.class_ or _.jar_), that are required for the compilation of certain class, are not completely analysed by ProgQuery.
- After the compilation proccess a single overlapped graph containing these 7 structures is included in a Neo4j graph es.uniovi.reflection.progquery.database.
- Finally, you can connect to the DB and execute Cypher/Gremlin queries against it (different queries placed in `src\es.uniovi.reflection.progquery.database\queries` can be used as examples)