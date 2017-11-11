package dk.danamlund.quicklaunch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FuzzyMatchToolsTest {

	@Test
	public void testFuzzyMatch() {
		assertTrue(fuzzyMatch("foo", "foo"));
		assertTrue(fuzzyMatch("?f?o?o?", "foo"));
		assertFalse(fuzzyMatch("oof", "foo"));
		assertFalse(fuzzyMatch("bar", "foo"));

		assertTrue(fuzzyMatch("FOO", "foo"));
		assertTrue(fuzzyMatch("foo", "FOO"));
	}

	@Test
	public void fuzzyScore() {
		checkScoreHigherThan("foo", "?f?o?o?", "foo");
	}

	@Test
	public void fuzzyScoreBetterToMatchFirstChar() {
		checkScoreHigherThan("foo", "?foo", "foo");
		checkScoreHigherThan("foo", "foobar", "foo");
	}

	@Test
	public void fuzzyScoreBetterToMatchLastChar() {
		checkScoreHigherThan("foo", "foo?", "foo");
	}

	@Test
	public void fuzzyScoreBetterToMatchCase() {
		checkScoreHigherThan("Foo", "foo", "Foo");
	}

	private void checkScoreHigherThan(String a, String b, String pattern) {
		assertTrue(a + "(" + getFuzzyScore(a, pattern) + ") > " + b + "(" + getFuzzyScore(b, pattern) + ")",
				getFuzzyScore(a, pattern) > getFuzzyScore(b, pattern));
	}

	private int getFuzzyScore(String element, String pattern) {
		return new StringFuzzyMatcher().getFuzzyScore(element, pattern);
	}

	private boolean fuzzyMatch(String element, String pattern) {
		return new StringFuzzyMatcher().fuzzyMatch(element, pattern);
	}
}
