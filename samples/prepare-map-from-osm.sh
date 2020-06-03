#!/bin/bash
while getopts ":i:k:o:" opt; do
  case $opt in
    i) INPUT_FILE="$OPTARG"
    ;;
    k) KEEP_OPTIONS="$OPTARG"
    ;;
    o) MAPSFORGE_OPTIONS="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done


if [ "$INPUT_FILE" = "" ] ; then
  echo "Prepares an .osm for distribution with the ARIONAV framework."
  echo ""
  echo " -i <INPUT_FILE>"
  echo "    Mandatory input file parameter."
  echo ""
  echo " -o '<MAPSFORGE_OPTIONS>'"
  echo "    Optional arguments for the mapsforge file writer. Should be single-quoted. Defaults to no options."
  echo ""
  echo " -k '<OSMFILTER KEEP OPTIONS>"
  echo "    Optional arguments for the osmfilter --keep argument. Should be single-quoted. Defaults to all."
  echo ""
  echo " -h"
  echo "    Prints this help."
  exit 1
fi

FULL_FILE_PATH=$(realpath $INPUT_FILE)
INPUT_FOLDER=$(dirname FULL_FILE_PATH)
NAME=$(basename $FULL_FILE_PATH)
NAME_WITHOUT_EXTENSION=${NAME::-4}
GH_FOLDER="$INPUT_FOLDER/${NAME_WITHOUT_EXTENSION}-gh/"
DISTRIBUTED_OSM_FILE="$GH_FOLDER/$NAME"



rm -rvf ${GH_FOLDER}/*
java -jar $HOME/projects/graphhopper/level-extension-importer/target/level-extension-importer-0.5-jar-with-dependencies.jar $FULL_FILE_PATH $GH_FOLDER
if [ $? -eq 0 ]
then
  echo "Successfully imported into GraphHopper."
else
  echo "Import failed." >&2
  exit 1
fi


echo "Executing SED to fix and copy osm file. May take a long time..."
date > SED.log
./sed_fix_osm.sh $FULL_FILE_PATH $DISTRIBUTED_OSM_FILE
if [ $? -eq 0 ]
then
  echo "sed fix was successful."
else
  echo "Could not sed fix file" >&2
  exit 1
fi


date >> SED.log
date > $GH_FOLDER/_timestamp


echo "Creating mapsforge file with mapsforge options: '$MAPSFORGE_OPTIONS'"
osmosis --rx file="$DISTRIBUTED_OSM_FILE" compressionMethod=none enableDateParsing=false --mw $MAPSFORGE_OPTIONS file="$GH_FOLDER/${NAME_WITHOUT_EXTENSION}.map"
if [ $? -eq 0 ]
then
  echo "Successfully created map file."
else
  echo "Could not create map file." >&2
  echo "If the error is 'Unable to unzip gz file.', use `xmllint $DISTRIBUTED_OSM_FILE` to check for any xml errors and adapt sed_fix_osm.sh accordingly." >&2
  exit 1
fi



if [ -z ${KEEP_OPTIONS+x} ]; then 
  echo "No keep options given. OSM file will not be filtered."
else
  echo "Removing non-relevant data from distributed osm file..."
  ./bin/osmfilter32 "$DISTRIBUTED_OSM_FILE" --keep="level= and indoor= or tourism=" > "${DISTRIBUTED_OSM_FILE}.indoor"

  if [ $? -eq 0 ]
  then 
    echo "Filtered file successfully."
  else
    echo "osmfilter32 failed." >&2
    exit 1
  fi

  ls -lh ${GH_FOLDER}/${NAME}*
  mv -v "${DISTRIBUTED_OSM_FILE}.indoor" "$DISTRIBUTED_OSM_FILE"
fi

pushd $GH_FOLDER
  zip "${NAME_WITHOUT_EXTENSION}.ghz" *
  OUTPUT_FILE_FULL_PATH=$(realpath "${NAME_WITHOUT_EXTENSION}.ghz")
popd

echo "Output file is located at $OUTPUT_FILE_FULL_PATH"

