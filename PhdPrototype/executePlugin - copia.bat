set /P PACKAGE="Introduce class(es) to compile"
echo %PACKAGE%
javac -cp .;"build/libs/*" -Xplugin:WiggleIndexerPlugin %PACKAGE%