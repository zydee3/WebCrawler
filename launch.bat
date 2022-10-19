@echo off
@title Spider
set CLASSPATH=.;out\artifacts\Spider_jar\*
java -Dlog4j.configurationFile=log4j.xml Spider
pause
