package com.github.krs.xrepo;

import com.github.krs.xrepo.action.DisabledBranches;
import com.github.krs.xrepo.action.FallbackBranches;

public class XRepoConfiguration {
    private boolean enabled = false;
    private String currentBranch = "";
    private String versionSuffix = "";
    private DisabledBranches disabledBranches = new DisabledBranches();
    private FallbackBranches fallbacks = new FallbackBranches();

    public void enabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void currentBranch(final String name) {
        currentBranch = name;
        versionSuffix = toVersionSuffix(currentBranch);
    }

    public void disabledBranches(final String... branches) {
        disabledBranches.add(branches);
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
        return disabledBranches.isDisabled(currentBranch);
    }

    private static String toVersionSuffix(final String name) {
        return "-" + name.replaceAll("/", "-");
    }

}
