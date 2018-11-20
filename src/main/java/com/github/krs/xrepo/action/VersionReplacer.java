package com.github.krs.xrepo.action;

import com.github.krs.xrepo.XRepoConfiguration;

import org.gradle.api.Project;

public class VersionReplacer extends AbstractProjectAction {

    @Override
    protected void execute(final XRepoConfiguration config, final Project project) {
        if (project.getVersion() != null) {
            project.setVersion(project.getVersion().toString() + config.getVersionSuffix());
            info(project, "Setting version to {}", project.getVersion());
        } else {
            warn(project, "Version is not set, cannot add branch suffix");
        }
    }

}
