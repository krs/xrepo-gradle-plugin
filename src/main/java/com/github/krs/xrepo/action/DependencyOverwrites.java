package com.github.krs.xrepo.action;

import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.artifacts.ResolvedDependency;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class DependencyOverwrites {
    private Set<Dependency> dependencies = new HashSet<>();

    public void add(final ResolvedDependency dep) {
        dependencies.add(new Dependency(dep));
    }

    public boolean contains(final DependencyResolveDetails dependency, final String overwrittenVersion) {
        return dependencies.contains(new Dependency(dependency, overwrittenVersion));
    }

    private static class Dependency {
        private String group;
        private String name;
        private String version;

        Dependency(final ResolvedDependency dep) {
            this.group = dep.getModuleGroup();
            this.name = dep.getModuleName();
            this.version = dep.getModuleVersion();
        }

        Dependency(final DependencyResolveDetails dep, final String version) {
            this.group = dep.getRequested().getGroup();
            this.name = dep.getRequested().getName();
            this.version = version;
        }

        @Override
        public int hashCode() {
            return Objects.hash(group, name, version);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            final Dependency other = (Dependency) obj;
            return Objects.equals(group, other.group)
                    && Objects.equals(name, other.name)
                    && Objects.equals(version, other.version);
        }

    }
}
