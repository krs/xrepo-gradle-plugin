package com.github.krs.xrepo.action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class FallbackBranches {

    private Map<BranchPattern, String> fallbacks = new LinkedHashMap<>();

    public void add(final String branchPattern, final String fallbackPattern) {
        fallbacks.put(new BranchPattern(branchPattern), fallbackPattern);
    }

    public String getFallbackBranch(final String branch) {
        for (Map.Entry<BranchPattern, String> entry: fallbacks.entrySet()) {
            final Matcher matcher = entry.getKey().matcher(branch);
            if (matcher.matches()) {
                if (matcher.groupCount() > 0) {
                    return entry.getValue().replace("[1]", matcher.group(1));
                } else {
                    return entry.getValue();
                }
            }
        }
        return "";
    }

}
