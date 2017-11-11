package dk.danamlund.quicklaunch;

import java.util.Comparator;

public class StringFuzzyMatcher {

	/**
	 * fuzzy 'foo' is same as regular '*f*o*o*'.
	 */
	public boolean fuzzyMatch(String element, String pattern) {
		if (pattern.isEmpty()) {
			return true;
		}
		pattern = pattern.toLowerCase();
		String s = element.toLowerCase().trim();
		int sIndex = 0;
		for (int i = 0; i < pattern.length(); i++) {
			if (sIndex >= s.length()) {
				return false;
			}
			sIndex = s.indexOf(pattern.charAt(i), sIndex);
			if (sIndex < 0) {
				return false;
			}
			sIndex++;
		}
		return true;
	}

	public int getFuzzyScore(String name, String filter) {
		String nameLower = name.toLowerCase();
		String filterLower = filter.toLowerCase();

		int score = 0;
		int nameI = 0;
		for (int filterI = 0; filterI < filter.length(); filterI++) {
			int newNameI = nameLower.indexOf(filterLower.charAt(filterI), nameI);
			if (newNameI == nameI) {
				score += 2;
				if (name.charAt(nameI) == filter.charAt(filterI)) {
					score++;
				}
			}
			nameI = newNameI + 1;
		}
		// Give score if final char of filter is also final char of name
		if (nameI == name.length()) {
			score += 2;
		}
		return score;
	}

	public Comparator<Object> getFuzzyScoreComparator(String pattern) {
		return new FuzzyScoreComparator(this, pattern);
	}

	private static class FuzzyScoreComparator implements Comparator<Object> {
		private final StringFuzzyMatcher stringFuzzyMatcher;
		private final String pattern;

		public FuzzyScoreComparator(StringFuzzyMatcher stringFuzzyMatcher, String pattern) {
			this.stringFuzzyMatcher = stringFuzzyMatcher;
			this.pattern = pattern;
		}

		@Override
		public int compare(Object aObject, Object bObject) {
			String a = aObject.toString();
			String b = bObject.toString();
			int comparison = Integer.compare(stringFuzzyMatcher.getFuzzyScore(b, pattern),
					stringFuzzyMatcher.getFuzzyScore(a, pattern));
			if (comparison != 0) {
				return comparison;
			}
			return a.compareTo(b);
		}
	}
}
