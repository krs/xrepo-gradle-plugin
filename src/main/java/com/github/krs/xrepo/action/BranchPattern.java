package com.github.krs.xrepo.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BranchPattern {
    private static final String MATCH_ALL = "~";
    private Pattern pattern;

    BranchPattern(final String pattern) {
        this.pattern = toRegex(pattern);
    }

    Matcher matcher(final String input) {
        return pattern.matcher(input);
    }

    boolean matches(final String input) {
        return matcher(input).matches();
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
