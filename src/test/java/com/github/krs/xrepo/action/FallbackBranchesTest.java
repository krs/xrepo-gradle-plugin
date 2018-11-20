package com.github.krs.xrepo.action;

import org.gradle.internal.impldep.org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

public class FallbackBranchesTest {
    private FallbackBranches fallbacks;

    @BeforeEach
    public void setUp() {
        fallbacks = new FallbackBranches();
    }

    @Test
    public void fallbackBranchIsEmptyWhenNothingConfigured() {
        expectFallback(RandomStringUtils.random(12), "");
    }

    @Test
    public void fallbackBranchesAreConsideredInOrderTheyWereConfigured() {
        final String branch = "feature/branch";
        fallbacks.add(branch, "first");
        fallbacks.add(branch, "second");

        expectFallback(branch, "first");
    }

    @Test
    public void fallbackBranchCopiesMatchedTilde() {
        fallbacks.add("feature/~/one", "[1]");
        fallbacks.add("feature/~/two", "notCopied");
        fallbacks.add("~/start", "start/[1]");
        fallbacks.add("end/~", "1[1]1");
        fallbacks.add("feature/~/~", "tilde/[1]");

        expectFallback("feature/match/one", "match");
        expectFallback("feature/match/two", "notCopied");
        expectFallback("feature/start", "start/feature");
        expectFallback("end/feature", "1feature1");
        expectFallback("feature/match/it", "tilde/match");
    }

    @Test
    public void fallbackBranchEscapesRegexChars() {
        fallbacks.add("a~.*~", "1");
        fallbacks.add("?.*.+[a-z]\\w*~", "2");
        fallbacks.add("~", "3");

        expectFallback("aa", "3");
        expectFallback("aa.*bb", "1");
        expectFallback("?.*.+[a-z]\\w*bb", "2");
    }

    @Test
    public void cannotUseTwoTildesNotSepareted() {
        assertThatThrownBy(() -> fallbacks.add("a~~b", "[1]")).isInstanceOf(IllegalArgumentException.class);
    }

    private void expectFallback(final String branch, final String fallback) {
        assertThat(fallbacks.getFallbackBranch(branch)).isEqualTo(fallback);
    }
}
