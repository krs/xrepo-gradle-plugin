package com.github.krs.xrepo.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BuildRunner {
    private File buildDir;
    private String file;
    private List<String> lines = new LinkedList<>();
    private String repository;
    private String dependencyConfiguration;
    private String dependency;

    public BuildRunner(final File buildDir) {
        this.buildDir = buildDir;
    }

    public BuildRunner withFile(final String file) {
        this.file = file;
        return this;
    }

    public BuildRunner withLines(final String... lines) {
        this.lines.add("\n");
        this.lines.addAll(Arrays.asList(lines));
        return this;
    }

    public BuildRunner withDependency(final String configuration, final String dependency) {
        this.dependencyConfiguration = configuration;
        this.dependency = dependency;
        return this;
    }

    public BuildRunner withRepository(final String repository) {
        this.repository = repository.replace('\\', '/');
        return this;
    }

    public BuildResult build() throws Exception {
        createProject();
        return GradleRunner.create()
                .withProjectDir(buildDir)
                .withPluginClasspath()
                .withArguments("build")
                .withDebug(true)
                .build();
    }

    private void createProject() throws Exception {
        createBuildFile();
        createSrcFile("src/main/java");
        createSrcFile("src/test/java");
    }

    private void createSrcFile(final String folder) throws Exception {
        final File srcDir = new File(buildDir, folder);
        srcDir.mkdirs();
        createFile(srcDir, "HelloWorld.java");
    }

    private void createBuildFile() throws Exception {
        final File buildFile = createFile(buildDir, "build.gradle");

        String build = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8);

        build = replace(build, "${CONFIGURATION}", dependencyConfiguration);
        build = replace(build, "${REPOSITORY_DIR}", repository);
        final StringBuilder buildContents = new StringBuilder(build);

        addDependency(buildContents);
        lines.forEach(l -> buildContents.append(l).append("\n"));

        FileUtils.write(buildFile, buildContents.toString(), StandardCharsets.UTF_8);
    }

    private String replace(final String build, final String key, final String value) {
        if (value != null) {
            return build.replace(key, value);
        }
        return build;
    }

    private void addDependency(final StringBuilder build) {
        if (dependency != null) {
            build.append("\ndependencies {\n");
            build.append(dependencyConfiguration).append(" '").append(dependency).append("'\n");
            build.append("}\n");
        }
    }

    private File createFile(final File dir, final String name) throws Exception {
        final File newFile = new File(dir, name);
        if (!newFile.createNewFile()) {
            throw new IOException("Cannot create new file " + name);
        }
        return newFile;
    }
}
