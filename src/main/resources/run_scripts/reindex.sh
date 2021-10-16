#!/bin/bash

REINDEXER_HOME_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd $REINDEXER_HOME_DIR

echo ###################################################################
echo Reindexer home folder: $REINDEXER_HOME_DIR
echo ###################################################################
echo #
cp $REINDEXER_HOME_DIR/config/reindex.yml $REINDEXER_HOME_DIR/client/assets/

java -Dlog4j2.configurationFile=$REINDEXER_HOME_DIR/config/log4j2.xml -cp $REINDEXER_HOME_DIR/lib/*:$REINDEXER_HOME_DIR/bin/* com.dbeast.reindex.Reindex $REINDEXER_HOME_DIR


