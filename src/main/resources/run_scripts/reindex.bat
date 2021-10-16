@echo off

set REINDEXER_HOME_DIR=%~dp0


ECHO ###################################################################
ECHO Reindexer home folder: %REINDEXER_HOME_DIR%
ECHO ###################################################################
ECHO #

copy %REINDEXER_HOME_DIR%\config\reindex.yml %REINDEXER_HOME_DIR%\client\assets

java -Dlog4j2.configurationFile=%REINDEXER_HOME_DIR%\config\log4j2.xml -cp %REINDEXER_HOME_DIR%\lib\*;%REINDEXER_HOME_DIR%\bin\* com.dbeast.reindex.Reindex %REINDEXER_HOME_DIR%

PAUSE