package com.github.krs.xrepo.action;

import com.github.krs.xrepo.XRepoConfiguration;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyResolveDetails;

import java.util.Objects;

public class DependencyReplacer extends AbstractProjectAction {
    private static final String COMPILE_CLASSPATH = "compileClasspath";
    private static final String TEST_COMPILE = "testCompile";

    @Override
    protected void execute(final XRepoConfiguration config, final Project project) {
        final Configuration compileClasspath = configuration(COMPILE_CLASSPATH, project);
        final Configuration testCompile = configuration(TEST_COMPILE, project);

        if (compileClasspath != null && testCompile != null) {
            final DependencyOverwrites dependencyOverwrites = collectDependenciesToOverwrite(compileClasspath, testCompile, config, project);
            project.getConfigurations().forEach(conf -> overwriteDependencies(conf, dependencyOverwrites, config, project));
        } else {
            warn(project, "Java plugin was not used, cannot overwrite dependencies");
        }
    }

    private static DependencyOverwrites collectDependenciesToOverwrite(final Configuration compileClasspath, final Configuration testCompile,
                                                                       final XRepoConfiguration config, final Project project) {
        final Configuration groupDeps = dependenciesWithinProjectGroup(testCompile, project);
        debug(project, "Found {} dependencies with group {}", groupDeps.getAllDependencies().size(), project.getGroup());
        useSuffixedVersions(groupDeps, config, project);

        final DependencyOverwrites result = new DependencyOverwrites();
        compileClasspath.getIncoming().beforeResolve((deps) -> {
            groupDeps.getResolvedConfiguration().getLenientConfiguration().getAllModuleDependencies().forEach(dep -> {
                if (isInSameGroup(dep.getModuleGroup(), project)) {
                    debug(project, "Found overwritten dependency {}:{}:{}", dep.getModuleGroup(), dep.getModuleName(), dep.getModuleVersion());
                    result.add(dep);
                }
            });
        });
        return result;

    }

    private static void useSuffixedVersions(final Configuration groupDeps, final XRepoConfiguration config, final Project project) {
        groupDeps.getResolutionStrategy().eachDependency(dependency -> {
            if (isInSameGroup(dependency, project) && !versionEndsWith(dependency, config.getVersionSuffix())) {
                debug(project, "Will look for version suffixed {} for {}:{}:{}", config.getVersionSuffix(),
                        dependency.getRequested().getGroup(), dependency.getRequested().getName(), dependency.getRequested().getVersion());
                dependency.useVersion(suffixed(dependency, config.getVersionSuffix()));
            }
        });
    }

    private static void overwriteDependencies(final Configuration conf, final DependencyOverwrites dependencyOverwrites,
                                              final XRepoConfiguration config, final Project project) {
        conf.getResolutionStrategy().eachDependency(dependency -> {
            if (isInSameGroup(dependency, project)) {
                final String overwrittenVersion = suffixed(dependency, config.getVersionSuffix());
                if (dependencyOverwrites.contains(dependency, overwrittenVersion)) {
                    info(project, "Using overwritten dependency {}:{}:{}",
                            dependency.getRequested().getGroup(), dependency.getRequested().getName(), overwrittenVersion);
                    dependency.useVersion(overwrittenVersion);
                } else {
                    final String fallbackSuffix = config.getFallbackSuffix();
                    if (!fallbackSuffix.isEmpty()
                            && !versionEndsWith(dependency, config.getVersionSuffix())
                            && !versionEndsWith(dependency, fallbackSuffix)) {
                        final String fallbackVersion = suffixed(dependency, fallbackSuffix);
                        info(project, "Using fallback dependency {}:{}:{}",
                                dependency.getRequested().getGroup(), dependency.getRequested().getName(), fallbackVersion);
                        dependency.useVersion(fallbackVersion);
                    }
                }
            }
        });
    }

    private static boolean versionEndsWith(final DependencyResolveDetails dependency, final String versionSuffix) {
        return dependency.getRequested().getVersion().endsWith(versionSuffix);
    }

    private static String suffixed(final DependencyResolveDetails dependency, final String suffix) {
        return dependency.getRequested().getVersion() + suffix;
    }

    private static Configuration dependenciesWithinProjectGroup(final Configuration testCompile, final Project project) {
        return testCompile.copyRecursive(dep -> isInSameGroup(dep.getGroup(), project));
    }

    private static boolean isInSameGroup(final DependencyResolveDetails dependency, final Project project) {
        return isInSameGroup(dependency.getRequested().getGroup(), project);
    }

    private static boolean isInSameGroup(final String group, final Project project) {
        return Objects.equals(group, project.getGroup());
    }

    private static Configuration configuration(final String name, final Project project) {
        final Configuration conf = project.getConfigurations().getByName(name);
        if (conf == null) {
            warn(project, "{} configuration doesn't exist. Remember to \"apply plugin: 'java'\"", name);
        }
        return conf;
    }

}
