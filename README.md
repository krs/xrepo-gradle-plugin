# xrepo-gradle-plugin
Cross-repository Gradle plugin modifies dependency resolution to use artifacts that were built from the same branch even if they're in different repository.

The plugin makes it easy to have a team working on the same set of shared repositories - artifacts built from one person's feature branch will not collide with artifacts built from other branches. In addition dependencies built from other repositories will be used if they have matching branch name. 

The idea on how to solve the problem was taken from this Peter Niederwieser's answer in [this StackOverflow question](https://stackoverflow.com/questions/22779806/pick-version-with-branchname-as-classifier-with-gradle) 

## Usage

Plugin changes project version to suffix current branch name (version `1.0.0` becomes `1.0.0-develop` when built from branch `develop` or `1.0.0-feature-something` when build from `feature/something`.

Plugin operates on configurations added by `java` plugin, see SO link above for details. When building from a branch all dependencies from the same group as current project will be checked if they have version with the same branch suffix. If such version exists it will be used, if it doesn't exist a fallback version will be used (if configured). If fallback is not configured then originally requested version will be used.

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

#### xrepo.disabledBranches(String... names)
Even when plugin is enabled you can mark certain branches to not change artifact version (for example when building from `master` in [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)

```
xrepo.disabledBranches 'master', 'release'
```

#### xrepo.fallback(String branchPattern, Strng fallback)
This can be used to configure which branch version should be used for dependencies in case they don't have a version identical to current branch. The first parameter is a pattern that will be matched to current branch (use tilde `~` to match zero or more characters); the second parameter is a branch name that will be used as fallback (use `[1]` to use characters matched by first tilde in fallback pattern).

For example this fill configure fallbacks for Gitflow - when building branch `feature/example` all dependencies (within the same project group) will be checked if they also have `feature/example` version build. If there is no such version for a dependency then `develop` version will be used.
One the other hand `hotfix/example` assumes that it will be branched from `master` (and merged back to it), so all dependencies will also be taken from master if there they don't have `hotfix/example` version. 

```
xrepo.fallback 'hotfix/~', ''
xrepo.fallback 'subfeature/~/~', 'feature/[1]'
xrepo.fallback '~', 'develop'
```

## Notes

* The plugin will produce a lot of branch specific artifacts, the repository that stores them should have some kind of cleanup policy implemented.