@echo off

set REINDEX_HOME_DIR=%~dp0
set REINDEX_HOME_DIR=%REINDEX_HOME_DIR:~0,-1%

ECHO ###################################################################
ECHO Reindex home folder: %REINDEX_HOME_DIR%
ECHO ###################################################################
ECHO #

copy "%REINDEX_HOME_DIR%\config\reindex.yml" "%REINDEX_HOME_DIR%\client\assets"

java -Dlog4j2.configurationFile="%REINDEX_HOME_DIR%\config\log4j2.xml" -cp "%REINDEX_HOME_DIR%\lib\*";"%REINDEX_HOME_DIR%\bin\*" com.dbeast.reindex.Reindex "%REINDEX_HOME_DIR%"

PAUSE