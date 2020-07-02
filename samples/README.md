# Sample

## Privacy Note

German only: http://ironjan.de/arionav-sample-datenschutz.html

## How to update maps

 * Update the paderborn base map via `update-paderborn-osm.sh`
 * Create an indoor map via josm
 * Merge the maps via josm
 * Then use either `prepare_arionav_map.sh` or `prepare_tourism_map.sh` to create the distributable `.ghz`-files
 * Update the resources within the app source folder
