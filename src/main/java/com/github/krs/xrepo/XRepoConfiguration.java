package com.github.krs.xrepo;

import com.github.krs.xrepo.action.FallbackBranches;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class XRepoConfiguration {
    private boolean enabled = false;
    private String currentBranch = "";
    private String versionSuffix = "";
    private Set<String> disabledBranches = new HashSet<>();
    private FallbackBranches fallbacks = new FallbackBranches();

    public void enabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void currentBranch(final String name) {
        currentBranch = name;
        versionSuffix = toVersionSuffix(currentBranch);
    }

    public void disabledBranches(final String... branches) {
        if (isEmpty(branches)) {
            return;
        }
        disabledBranches.addAll(Arrays.asList(branches));
    }

    public void fallback(final String branchPattern, final String fallbackPattern) {
        fallbacks.add(branchPattern, fallbackPattern);
    }

    public boolean isEnabled() {
        return enabled && !currentBranchIsDisabled();
    }

    public String getVersionSuffix() {
        return versionSuffix;
    }

    public String getFallbackSuffix() {
        final String fallbackBranch = fallbacks.getFallbackBranch(currentBranch);
        if ("".equals(fallbackBranch)) {
            return fallbackBranch;
        }
        return toVersionSuffix(fallbackBranch);
    }

    private boolean currentBranchIsDisabled() {
        return disabledBranches.contains(currentBranch);
    }

    private static String toVersionSuffix(final String name) {
        return "-" + name.replaceAll("/", "-");
    }

    private static <T> boolean isEmpty(final T[] array) {
        return array == null || array.length == 0;
    }
}
