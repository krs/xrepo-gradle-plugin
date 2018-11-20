package com.github.krs.xrepo.action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FallbackBranches {
    private static final String MATCH_ALL = "~";
    private Map<Pattern, String> fallbacks = new LinkedHashMap<>();

    public void add(final String branchPattern, final String fallbackPattern) {
        fallbacks.put(toRegex(branchPattern), fallbackPattern);
    }

    public String getFallbackBranch(final String branch) {
        for (Map.Entry<Pattern, String> entry: fallbacks.entrySet()) {
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

    private Pattern toRegex(final String branchPattern) {
        requireCorrectBranchPattern(branchPattern);
        String pattern = Pattern.quote(branchPattern);
        pattern = pattern.replace(MATCH_ALL, "\\E(.*)\\Q");
        pattern = removeStart("\\Q\\E", pattern);
        pattern = removeEnd("\\Q\\E", pattern);
        return Pattern.compile(pattern);
    }

    private String removeEnd(final String end, final String string) {
        if (string.endsWith(end)) {
            return string.substring(0, string.length() - end.length());
        }
        return string;
    }

    private String removeStart(final String start, final String string) {
        if (string.startsWith(start)) {
            return string.substring(start.length());
        }
        return string;
    }

    private void requireCorrectBranchPattern(final String branchPattern) {
        if (branchPattern == null || branchPattern.isEmpty()) {
            throw new IllegalArgumentException("Fallback pattern must not be empty");
        }
        if (branchPattern.contains(MATCH_ALL + MATCH_ALL)) {
            throw new IllegalArgumentException("Match-all patterns ~ must be separated.");
        }
    }
}
