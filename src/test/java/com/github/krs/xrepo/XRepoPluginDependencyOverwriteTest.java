package com.github.krs.xrepo;

import com.github.krs.xrepo.test.BuildRunner;
import com.github.krs.xrepo.test.MavenRepository;

import org.gradle.testkit.runner.BuildResult;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class XRepoPluginDependencyOverwriteTest {
    private static final String FALLBACK = "Using fallback dependency ";

    private static final String OVERWRITE = "Using overwritten dependency ";

    @Rule
    private final TemporaryFolder tempDir = new TemporaryFolder();

    private BuildRunner runner;
    private File buildDir;

    @BeforeEach
    public void setUp() throws Exception {
        tempDir.create();
        buildDir = tempDir.newFolder("project");

        final File repo = tempDir.newFolder("repository");
        MavenRepository.create(repo);
        runner = new BuildRunner(buildDir)
                .withFile("dependency.gradle")
                .withRepository(repo.getAbsolutePath());
    }

    @AfterEach
    public void tearDown() throws Exception {
        tempDir.delete();
    }

    @Test
    public void dependenciesAreSuffixedWhenPluginEnabled() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'develop'")
                .withDependency("compile", "testgroup:test-dep:1.0.0")
                .build();

        expectOverritten("testgroup:test-dep:1.0.0-develop", result);
    }

    @Test
    public void dependenciesAreNotSuffixedWhenPluginDisabled() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled false", "xrepo.currentBranch 'develop'")
                .withDependency("compile", "testgroup:test-dep:1.0.0")
                .build();

        expectNotOverritten(result);
    }

    @Test
    public void dependenciesAreNotSuffixedWhenPluginEnabledButCurrentBranchDisabled() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'develop'")
                .withLines("xrepo.disabledBranches '~e~'")
                .withDependency("compile", "testgroup:test-dep:1.0.0")
                .build();

        expectNotOverritten(result);
    }

    @Test
    public void dependencySuffixIsEscapedCurrentBranch() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'feature/1'")
                .withDependency("compile", "testgroup:test-dep:1.0.0")
                .build();

        expectOverritten("testgroup:test-dep:1.0.0-feature-1", result);
    }

    @Test
    public void testDependenciesMayBeAlsoSuffixed() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'develop'")
                .withDependency("testCompile", "testgroup:test-dep:1.0.0")
                .build();

        expectOverritten("testgroup:test-dep:1.0.0-develop", result);
    }

    @Test
    public void onlySameGroupDependenciesAreSuffixed() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'develop'")
                .withDependency("compile", "othergroup:othergroup-dep:1.0.0")
                .build();

        expectNotOverritten(result);
    }

    @Test
    public void transitiveDependenciesAreSuffixed() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'develop'")
                .withDependency("compile", "testgroup:transitive:1.0.0")
                .build();

        expectOverritten("testgroup:other-dep:1.0.0-develop", result);
        expectOverritten("testgroup:transitive:1.0.0-develop", result);
    }

    @Test
    public void fallbackDependencyUsedWhenSuffixedVersionNotFound() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'feature/2'")
                .withLines("xrepo.fallback 'feature/~', 'develop'")
                .withDependency("compile", "testgroup:test-dep:1.0.0")
                .build();

        expectFallback("testgroup:test-dep:1.0.0-develop", result);
    }

    @Test
    public void fallbackDependenciesConsideredInOrderItWasDefined() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'feature/2'")
                .withLines("xrepo.fallback 'feature/1', 'wrong'")
                .withLines("xrepo.fallback 'bugfix/~', 'wrong'")
                .withLines("xrepo.fallback 'feature/~', 'develop'")
                .withDependency("compile", "testgroup:test-dep:1.0.0")
                .build();

        expectFallback("testgroup:test-dep:1.0.0-develop", result);
    }

    @Test
    public void fallbackDependencyMayCaptureAllBranches() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'feature/2'")
                .withLines("xrepo.fallback '~', 'develop'")
                .withDependency("compile", "testgroup:test-dep:1.0.0")
                .build();

        expectFallback("testgroup:test-dep:1.0.0-develop", result);
    }

    @Test
    public void fallbackDependenMayBeCalculatedUsingPatternMatching() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'feature/develop/something'")
                .withLines("xrepo.fallback 'feature/~/~', '[1]'")
                .withDependency("compile", "testgroup:test-dep:1.0.0")
                .build();

        expectFallback("testgroup:test-dep:1.0.0-develop", result);
    }

    @Test
    public void originalDependencyUsedWhenSuffixedVersionNotFoundAndNoFallbackDefined() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'feature/develop/something'")
                .withDependency("compile", "testgroup:test-dep:1.0.0")
                .build();

        expectNotOverritten(result);
    }

    @Test
    public void fallbackDependencyUsedForTransitiveDependencies() throws Exception {
        final BuildResult result = runner
                .withLines("xrepo.enabled true", "xrepo.currentBranch 'feature/something'")
                .withLines("xrepo.fallback 'feature/~', 'develop'")
                .withDependency("compile", "testgroup:transitive:1.0.0")
                .build();

        expectFallback("testgroup:other-dep:1.0.0-develop", result);
        expectFallback("testgroup:transitive:1.0.0-develop", result);
    }

    private void expectFallback(final String dependency, final BuildResult result) {
        expectOutputAtLeastTwice(FALLBACK + dependency, result);
    }

    private void expectOverritten(final String dependency, final BuildResult result) {
        expectOutputAtLeastTwice(OVERWRITE + dependency, result);
    }

    private void expectOutputAtLeastTwice(final String expected, final BuildResult result) {
        // text is printed few times - once for compile, once for jar manifest, one for testCompile
        final String search = Pattern.quote(expected);
        final Pattern pattern = Pattern.compile(".*" + search + ".*" + search + ".*", Pattern.MULTILINE | Pattern.DOTALL);
        assertThat(result.getOutput()).matches(pattern);
    }

    private void expectNotOverritten(final BuildResult result) {
        assertThat(result.getOutput()).doesNotContain(OVERWRITE);
        assertThat(result.getOutput()).doesNotContain(FALLBACK);
    }
}
