[![Build Status](https://travis-ci.com/krs/xrepo-gradle-plugin.svg?branch=master)](https://travis-ci.com/krs/xrepo-gradle-plugin)
[![Coverage Status](https://coveralls.io/repos/github/krs/xrepo-gradle-plugin/badge.svg?branch=master)](https://coveralls.io/github/krs/xrepo-gradle-plugin?branch=master)

# Cross-repository dependencies Gradle plugin
Cross-repository Gradle plugin modifies dependency resolution to use artifacts that were built from matching branch even if they're in different repositories.

The plugin makes it easy to have a team working simultaneously on features that span a set of shared repositories - artifacts built from one person's feature branch will not collide with artifacts built from other branches. In addition dependencies built from other repositories will be used if they have a matching branch name. 

The idea on how to solve the problem was taken from Peter Niederwieser's answer in [this StackOverflow question](https://stackoverflow.com/questions/22779806/pick-version-with-branchname-as-classifier-with-gradle) 

## Behaviour

The plugin changes project version to suffix current branch name (version `1.0.0` becomes `1.0.0-develop` when built from branch `develop` or `1.0.0-feature-something` when build from `feature/something`.

The plugin operates on configurations added by `java` plugin. When building from a branch all dependencies from the same group as current project will be checked whether they have a version with matching suffix. If such a version exists it will be used, if it doesn't exist a fallback version will be used (if configured). If fallback is not configured then originally requested version will be used.

## Usage

For Gradle 2.1 and later:

```
plugins {
  id "com.github.krs.xrepo" version "1.0.0"
}
```

For older Gradle versions or where dynamic configuration is required:

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.github.krs.xrepo-gradle-plugin:xrepo-gradle-plugin:1.0.0"
  }
}

apply plugin: "com.github.krs.xrepo"
```

## Configuration

#### xrepo.enabled(boolean value)
Used to enable the plugin. It may not be very useful to have modified version on developer's local machine so this can be used to enable it only on Continuous Integration server (for example by checking that environment variable is set).

```
xrepo.enabled System.getenv('BUILD_NUMBER') != null  
```

#### xrepo.currentBranch(String name)
Used to set current branch name. Usually CI server will be able to inject it via environment variable.

```
xrepo.currentBranch System.getenv('GIT_BRANCH')
```

#### xrepo.disabledBranches(String... patterns)
Even when plugin is enabled you can mark certain branches to not change artifact version (for example when building from `master` in [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)). Tilde `~` may be used to match zero or more characters.

```
xrepo.disabledBranches 'master', 'release/~'
```

#### xrepo.fallback(String branchPattern, String fallback)
This can be used to configure which branch version should be used for dependencies in case they don't have a version identical to current branch.

The first parameter is a pattern that will be matched to current branch (use tilde `~` to match zero or more characters).

The second parameter is a branch name that will be used as fallback (use `[1]` to use characters matched by first tilde in fallback pattern). Order in which fallbacks are defined is important - first found match returns the fallback. If there is no match then dependency will use version defined in build script.

##### Example

```
xrepo.fallback 'hotfix/~', ''
xrepo.fallback 'subfeature/~/~', 'feature/[1]'
xrepo.fallback 'release/~', 'release/[1]'
xrepo.fallback '~', 'develop'
```
* The example above will configure fallbacks for Gitflow - when building branch `feature/example` all dependencies (within the same project group) will be checked if they also have `feature/example` version build. If there is no such version for a dependency then `develop` version will be used due to "capture-all" pattern `~` at the end.

* On the other hand `hotfix/example` assumes that it will be branched from `master` (and merged back to it), so all dependencies will also be taken from master if  they don't have `hotfix/example` version. Please note that there is a difference between having `xrepo.disabledBraches 'master'` and `xrepo.fallback 'mater', ''` - the second will still suffix version of current build (and use dependencies with the same suffix).

* Sub-feature branch example demonstrates how the plugin may be used when team works on a big change that is not merged to `develop`. First a "sink branch" for such feature is created `feature/big`, then each team member may start working on a part of in their own (sub)feature branches `subfeature/big/change-one`, `subfeature/big/change-two`, etc. Dependencies for artifacts build from each (sub)feature branch will fallback to `feature/big` branch version.

* Release fallback example shows how to enforce building all artifacts in correct order - all dependencies must be be build from the same release branch before current artifact can be built. Fallback points to the same branch that has already been checked and not found, this will cause the build to fail. 


## Notes

* The plugin will produce a lot of branch specific artifacts. Please remember to implement some kind of cleanup policy for the repository that stores them. 

## Contributions

Fell free to raise issues or send Pull Requests.
