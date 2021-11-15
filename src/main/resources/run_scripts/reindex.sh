#!/bin/bash

REINDEX_HOME_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd $REINDEX_HOME_DIR

echo ###################################################################
echo Reindex home folder: $REINDEX_HOME_DIR
echo ###################################################################
echo #
cp "$REINDEX_HOME_DIR/config/reindex.yml" "$REINDEX_HOME_DIR/client/assets/"

java -Dlog4j2.configurationFile="$REINDEX_HOME_DIR/config/log4j2.xml" -cp "$REINDEX_HOME_DIR/lib/*":"$REINDEX_HOME_DIR/bin/*" com.dbeast.reindex.Reindex "$REINDEX_HOME_DIR"


