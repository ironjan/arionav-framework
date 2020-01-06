# arionav-framework

Implementation of the framework for android

# Creating ghz files

Current state:

 * Import an osm-file via the level-extension-test module of https://git.cs.upb.de/mtljan/graphhopper
 * Adapt the `prepare-ghz.sh`-script in graphhopper to use the correct files and execute it
 * Add the resulting ghz-file as a raw resource to your app

Notes:

 * The original osm file needs to be added to the ghz-file for this framework
 * It also needs to contain a file called `_timestamp` that contains some kind of identifier to uniquely identify the ghz-files version/creation time