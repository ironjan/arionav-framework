MAP_FOLDER="$HOME/projects/mapping"
NAME="uni_paderborn"
OSM="$MAP_FOLDER/${NAME}.osm"
GH_FOLDER="$MAP_FOLDER/${NAME}-gh/"

rm -rv ${GH_FOLDER}/*
java -jar $HOME/projects/graphhopper/level-extension-importer/target/level-extension-importer-0.5-jar-with-dependencies.jar $OSM $GH_FOLDER
echo "Executing SED to fix and copy osm file. May take a long time..."
date > SED.log
sed -E "s/node /node version='1' /" $OSM \
 | sed -E "s/way /way version='1' /" \
 | sed -E "s/relation /relation version='1' /" \
 | sed -E "s/(.*)version='1'(.*)version='(.*)'(.*)/\1 \2 version='\3'\4/" \
 | sed -E "s/.*tag(.*)v='.*version='1'.*'.*//" \
 > "${GH_FOLDER}/${NAME}.osm" 
date >> SED.log


pushd $GH_FOLDER
ls

osmosis --rx file="${NAME}.osm" enableDateParsing=false --mw  file="${NAME}.map"
date > _timestamp
zip "${NAME}.ghz" *


popd

cp -v "$GH_FOLDER/${NAME}.ghz" src/main/res/raw/
