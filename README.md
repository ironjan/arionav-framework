# arionav-framework

Implementation of an Augmented Reality Indoor/Outdoor Navigation Framework for Android. Part of my master thesis. 

The framework is currently developed on my university's gitlab instance and automatically synchronized to gitlab.com/lippertsjan/arionav-framework.

# Adding the libraries

Add the jitpack repository to your root build.gradle at the end of repositories:

```
allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
  }
```

Then add the dependencies you need to your app:
```
implementation 'com.github.ironjan.arionav-framework:ionav:master-SNAPSHOT'
implementation 'com.gitlab.lippertsjan.arionav-framework:arionav-extension:master-SNAPSHOT'


// position provider implementations
implementation "com.github.ironjan.arionav-framework:gps-positioning-implementation:master-SNAPSHOT"
implementation "com.github.ironjan.arionav-framework:bluetooth-positioning-provider-implementation:master-SNAPSHOT"
implementation "com.github.ironjan.arionav-framework:wifi-positioning-provider-implementation:master-SNAPSHOT"
```


# Creating ghz files

## Pre-Requisites

 * GraphHopper with level extensions
   * Clone https://github.com/ironjan/graphhopper and check out the `gh-extensions` branch (`git clone -b gh-extensions git@github.com:ironjan/graphhopper.git`)
   * Install the grapphopper libraries in your local maven cache via `mvn -DskipTests install`
 * `sed` - Pre-Installed on most Linux distributions
 * `osmosis` - See https://wiki.openstreetmap.org/wiki/Osmosis for more details.
   * Many Linux distributions provide `osmosis` via their package manager

## How To

 * Download an `.osm`-file of your area or extract it from a bigger area.
  * [Geofabrik](https://www.geofabrik.de/data/download.html) provides regional extracts of osm data.
  * Use osmosis to extract the actual area: https://wiki.openstreetmap.org/wiki/Osmosis#Extracting_bounding_boxes

> bzcat downloaded.osm.bz2 | osmosis  --read-xml enableDateParsing=no file=-  --bounding-box top=49.5138 left=10.9351 bottom=49.3866 right=11.201 --write-xml file=- | bzip2 > extracted.osm.bz2

 * Afterwards, the `prepare-map-from-osm.sh` can be used to prepare a `.ghz` file
 * Add the resulting ghz-file as a raw resource to your app
 
