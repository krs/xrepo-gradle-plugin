package com.github.krs.xrepo;

import com.github.krs.xrepo.test.BuildRunner;

import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class XRepoPluginVersionOverwriteTest {
    @Rule
    private final TemporaryFolder tempDir = new TemporaryFolder();

    @BeforeEach
    public void setUp() throws Exception {
        tempDir.create();
    }

    @AfterEach
    public void tearDown() throws Exception {
        tempDir.delete();
    }

    @Test
    public void versionIsSuffixedWhenPluginEnabled() throws Exception {
        expectVersionForBuildFile("-develop", "xrepo.enabled true", "xrepo.currentBranch 'develop'");
    }

    @Test
    public void versionIsNotSuffixedWhenPluginDisabled() throws Exception {
        expectVersionForBuildFile("", "xrepo.enabled false", "xrepo.currentBranch 'develop'");
    }

    @Test
    public void versionIsNotSuffixesWhenPluginEnabledButCurrentBranchDisabled() throws Exception {
        expectVersionForBuildFile("", "xrepo.enabled true", "xrepo.currentBranch 'one'", "xrepo.disabledBranches 'one', 'two'");
    }

    @Test
    public void versionSuffixIsEscapedCurrentBranch() throws Exception {
        expectVersionForBuildFile("-feature-1", "xrepo.enabled true", "xrepo.currentBranch 'feature/1'");
    }

    private void expectVersionForBuildFile(final String versionSuffix, final String... buildFile) throws Exception {
        new BuildRunner(tempDir.getRoot())
                .withFile("version.gradle")
                .withLines(buildFile).build();

        assertThat(new File(tempDir.getRoot(), "build/libs/test-lib-1.0.0" + versionSuffix + ".jar")).exists();
    }

}
