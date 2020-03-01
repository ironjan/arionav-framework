MAP_FOLDER="$HOME/projects/mapping"
NAME="uni_paderborn"
OSM="$MAP_FOLDER/${NAME}.osm"
GH_FOLDER="$MAP_FOLDER/${NAME}-gh/"

java -jar $HOME/projects/graphhopper/level-extension-importer/target/level-extension-importer-0.5-jar-with-dependencies.jar $OSM $GH_FOLDER
sed -e "s/node /node version='1' /" -e "s/version='1'(.*)version='(.*)'/$1 version='$2'/" > "${GH_FOLDER}/${NAME}.osm" 


pushd $GH_FOLDER
ls

osmosis --rx file="${NAME}.osm" enableDateParsing=false --mw  file="${NAME}.map"
date > _timestamp
zip "${NAME}.ghz" *


popd

cp -v "$GH_FOLDER/${NAME}.ghz" src/main/res/raw/
