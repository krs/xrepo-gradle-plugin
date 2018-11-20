package com.github.krs.xrepo.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class MavenRepository {

    private static final String VERSION = "${VERSION}";
    private static final String GROUP = "${GROUP}";
    private static final String ARTIFACT = "${ARTIFACT}";

    public static void create(final File repoDir) throws Exception {
        final String pomTemplate = readFile("maven/simple.pom");

        createMavenEntry(repoDir, pomTemplate, "testgroup", "test-dep", "1.0.0");
        createMavenEntry(repoDir, pomTemplate, "testgroup", "test-dep", "1.0.0-develop");
        createMavenEntry(repoDir, pomTemplate, "testgroup", "test-dep", "1.0.0-feature-1");

        createMavenEntry(repoDir, pomTemplate, "testgroup", "other-dep", "1.0.0");
        createMavenEntry(repoDir, pomTemplate, "testgroup", "other-dep", "1.0.0-develop");

        createMavenEntry(repoDir, pomTemplate, "othergroup", "othergroup-dep", "1.0.0");
        createMavenEntry(repoDir, pomTemplate, "othergroup", "othergroup-dep", "1.0.0-develop");

        createMavenEntryWithPomFile(repoDir, "maven/transitive.pom", "testgroup", "transitive", "1.0.0");
        createMavenEntryWithPomFile(repoDir, "maven/transitive-develop.pom", "testgroup", "transitive", "1.0.0-develop");
    }

    private static String readFile(final String file) throws Exception {
        return IOUtils.toString(MavenRepository.class.getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8);
    }

    private static void createMavenEntry(final File repoDir, final String pomTemplate,
                                         final String group, final String artifact, final String version)
            throws Exception {
        final String pom = pomTemplate
                .replace(VERSION, version)
                .replace(ARTIFACT, artifact)
                .replace(GROUP, group);

        createMavenEntryWithPomContents(repoDir, pom, group, artifact, version);
    }

    private static void createMavenEntryWithPomFile(final File repoDir, final String pomFile,
                                                    final String group, final String artifact, final String version)
            throws Exception {
        createMavenEntryWithPomContents(repoDir, readFile(pomFile), group, artifact, version);
    }

    private static void createMavenEntryWithPomContents(final File repoDir, final String pomContents,
                                                        final String group, final String artifact, final String version)
            throws Exception {
        final File artifactDir = new File(repoDir, group + "/" + artifact + "/" + version);
        artifactDir.mkdirs();

        final File jar = new File(artifactDir, artifact + "-" + version + ".jar");
        FileUtils.copyInputStreamToFile(MavenRepository.class.getClassLoader().getResourceAsStream("maven/empty.jar"), jar);

        final File pom = new File(artifactDir, artifact + "-" + version + ".pom");
        FileUtils.write(pom, pomContents, StandardCharsets.UTF_8);
    }

}
