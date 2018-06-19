javac -cp src;build/libs/*;build/libs/neo4j/*; -Xplugin:WiggleIndexerPlugin -d bon testClasses\CFG\exceptionThrows\*.java
pause