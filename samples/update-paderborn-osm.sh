#!/bin/bash
wget http://download.geofabrik.de/europe/germany/nordrhein-westfalen/detmold-regbez-latest.osm.bz2 -O - \
| bzcat - \
| osmosis --read-xml enableDateParsing=no file=- \
  --bounding-box top=51.73935 left=8.72873 bottom=51.69914 right=8.78597 \
  --write-xml file=paderborn.osm
