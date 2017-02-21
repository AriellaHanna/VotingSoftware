set +v
title Initializer

javac -sourcepath ../init ../init/*.java
start "Initializer" /D"../init" java Initializer