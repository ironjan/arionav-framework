MAP_FOLDER="$HOME/projects/maps"
NAME="uni_paderborn"
OSM="$MAP_FOLDER/${NAME}.osm"
GH_FOLDER="$MAP_FOLDER/${NAME}-gh/"

java -jar $HOME/projects/graphhopper/level-extension-importer/target/level-extension-importer-0.5-jar-with-dependencies.jar $OSM $GH_FOLDER
cp -v $OSM $GH_FOLDER

pushd $GH_FOLDER
ls
osmosis --rx file="${NAME}.osm" enableDateParsing=false --mw  file="${NAME}.map"
zip "${NAME}.ghz" *
popd

cp -v "$GH_FOLDER/${NAME}.ghz" src/main/res/raw/
