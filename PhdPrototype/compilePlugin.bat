jar cfe Wiggle.jar WiggleIndexerPlugin -C bin/ . build\libs\*
xcopy Wiggle.jar build\libs\Wiggle.jar  < yes.txt
del Wiggle.jar