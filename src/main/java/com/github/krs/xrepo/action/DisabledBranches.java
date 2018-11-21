package com.github.krs.xrepo.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DisabledBranches {
    private Set<BranchPattern> branches = new HashSet<>();

    public void add(final String... branches) {
        if (branches == null) {
            return;
        }
        Arrays.stream(branches)
                .map(BranchPattern::new)
                .forEach(this.branches::add);
    }

    public boolean isDisabled(final String branch) {
        return branches.stream().anyMatch(b -> b.matches(branch));
    }

}
