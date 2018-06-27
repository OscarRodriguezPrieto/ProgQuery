# ProgQuery
ProgQuery is a system to extract useful syntactic and semantic information from source code programs and store it in a graph database for posterior querying. 
For each program or compilation unit, ProgQuery extracts the following graph structures:
- Abstract Syntax Tree
- Call Graph
- Type Hierarchy Graph
- Control Flow Graph
- Program Dependency Graph
- Class Dependency Graph
# Build Instructions
- JDK8 or higher

# How to use
Currently, ProgQuery is only avaliable as a plugin for the javac compiler.  
In order to use it to analyse programs during the compilation process:
- Download the content of the _binaries_ folder: _ProgQuery.jar_ and the jars placed in _lib_.
- Run this command to compile and analyse Java classes   
`javac -cp ProgQuery.jar;libs;<other_source_or_binary_files_necessary_for_compilation> -Xplugin:ProgQueryPlugin <directory>\<YourClass>.java`  
If during the compilation of certain class, it is necessary to compile another classes, all these compiled classes are going to be analysed and included in the database.
  On the other hand, binary files (_.class_ or _.jar_), that are required for the compilation of certain class, are not completely analysed by ProgQuery.
- After the compilation proccess a single overlapped graph containing these 6 structures and semantic information is included in a Neo4j (version 3.3.4) graph database.
  This database is placed in the directory `./neo4j/data/ProgQuery.db/`
- Finally, you can connect to the DB and execute Cypher queries against it (many different queries placed in `src\database\queries` can be used as examples)