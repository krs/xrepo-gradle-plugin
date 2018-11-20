package com.github.krs.xrepo.action;

import com.github.krs.xrepo.XRepoConfiguration;

import org.gradle.api.Action;
import org.gradle.api.Project;

abstract class AbstractProjectAction implements Action<Project> {

    protected abstract void execute(XRepoConfiguration config, Project project);

    @Override
    public void execute(final Project project) {
        final XRepoConfiguration config = project.getExtensions().getByType(XRepoConfiguration.class);
        if (config.isEnabled()) {
            execute(config, project);
        }
    }

    protected static void warn(final Project project, final String message, final Object... args) {
        log(project, "[WARN] " + message, args);
    }

    protected static void info(final Project project, final String message, final Object... args) {
        log(project, message, args);
    }

    protected static void debug(final Project project, final String message, final Object... args) {
        //log(project, message, args);
        project.getLogger().debug(message, args);
    }

    private static void log(final Project project, final String message, final Object[] args) {
        project.getLogger().lifecycle("xrepo: " + message, args);

    }

}
