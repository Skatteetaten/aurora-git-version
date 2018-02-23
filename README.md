
# Introduction

aurora-git-version is a library for determining a version (typically an application or library version) from
information found in Git.

Its primary use case is as the functional component in custom build plugins (maven and gradle) that will use it for
setting the version of the project that is being built. There is also a CLI that can be used to determine the version
of any Git project if build plugins cannot be used - for instance when building node.js applications.


## How the version is determined

The basic rule that is used to determine the current version is to get the git tag of the current commit and use the
name of that tag (excluding a prefix, default ```v```) as the current version. So, for example, if you are building
a commit that is tagged with ```v1.0.0```, ```1.0.0``` is used as the version. 

Most of the time, though, in a CI/CD environment, the commit currently being built will not be tagged with a version
tag. The default behaviour then is to use the name of the current branch postfixed with a label (default 
```-SNAPSHOT```). So, for example, if you are building from the branch ```develop``` and have not tagged the current
commit, the version will become ```develop-SNAPSHOT```. Characters like ```/``` and ```-``` will be replaced by
```_```, so a branch called ```feature/PROJ-458-some-new-feature``` will get the version 
```feature_PROJ_458_some_new_feature```.

If you are performing a build from a commit that has not been tagged, but you do not want a ```-SNAPSHOT``` version,
the library support suggesting the next [semver](http://semver.org) compatible version based on the current set of
version tags and a version hint. So, for example, if you are building from ```master```, have enabled version
suggestions for the master branch, have already tagged previous commits with ```v1.0.0``` and ```v1.0.1``` and provide
the hint ```1.0-SNAPSHOT```, the version will become ```1.0.2```.

The library does not tag and push the suggested version, so to avoid suggesting the same non-snapshot version for a
future commit you are responsible yourself (or by the support of plugins) to tag the current commit appropriately and
push the new tag.

