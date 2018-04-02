set /P PACKAGE="Introduce class(es) to compile"
echo %PACKAGE%
javac -cp bin;"build/libs/*" -Xplugin:WiggleIndexerPlugin %PACKAGE%