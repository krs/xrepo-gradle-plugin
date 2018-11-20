package com.github.krs.xrepo;

import com.github.krs.xrepo.action.DependencyReplacer;
import com.github.krs.xrepo.action.VersionReplacer;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class XRepoPlugin implements Plugin<Project> {
    private static final String XREPO_EXTENSION = "xrepo";

    @Override
    public void apply(Project project) {
        project.getExtensions().create(XREPO_EXTENSION, XRepoConfiguration.class);
        project.afterEvaluate(new VersionReplacer());
        project.afterEvaluate(new DependencyReplacer());
    }

}
