javac -cp src;build/libs/*;build/libs/neo4j/*; -Xplugin:WiggleIndexerPlugin -d bon src\examples\MIG\*.java
pause