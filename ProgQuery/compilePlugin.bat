rmdir /Q /S bin\examples
jar cfe Wiggle.jar WiggleIndexerPlugin -C bin/ . build\libs\*
del build\libs\Wiggle.jar
xcopy Wiggle.jar build\libs\Wiggle.jar  < file.txt
del Wiggle.jar
pause