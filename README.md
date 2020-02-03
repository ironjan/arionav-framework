# arionav-framework

Implementation of an Augmented Reality Indoor/Outdoor Navigat Framework for Android. Part of my master thesis. 

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
implementation 'com.gitlab.lippertsjan.arionav-framework:ionav:master-SNAPSHOT'
implementation 'com.gitlab.lippertsjan.arionav-framework:arionav-extension:master-SNAPSHOT'
```


# Creating ghz files

Current state:

 * Import an osm-file via the level-extension-test module of https://git.cs.upb.de/mtljan/graphhopper
 * Adapt the `prepare-ghz.sh`-script in graphhopper to use the correct files and execute it
 * Add the resulting ghz-file as a raw resource to your app

Notes:

 * The original osm file needs to be added to the ghz-file for this framework
 * It also needs to contain a file called `_timestamp` that contains some kind of identifier to uniquely identify the ghz-files version/creation time
