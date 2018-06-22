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
- Download _ProgQuery.jar_ and _libs_ folder with all the jars contained in it.
- Run this command to compile and analyse Java classes   
`javac -cp ProgQuery.jar;libs;<other_source_or_binary_files_necessary_for_compilation> -Xplugin:ProgQueryPlugin <directory>:<YourClass>.java`  
If during the compilation of certain class, it is necessary to compile another classes, all these compiled classes are going to be analysed and included in the database.
  On the other hand, binary files (.class or .jar), that are required for the compilation of certain class, are not analysed by ProgQuery.
- After the compilation proccess a single overlapped graph containing these 6 structures and semantic information is included in a neo4j graph database.
  This database is place in the directory `./neo4j/data/ProgQuery.db/`
  
# Example queries