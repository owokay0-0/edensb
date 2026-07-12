package dev.eden.client.update;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class VersionComparator {
	private VersionComparator() {
	}

	public static boolean isNewer(String candidate, String current) {
		return compare(candidate, current) > 0;
	}

	public static int compare(String left, String right) {
		List<Token> leftTokens = tokenize(normalize(left));
		List<Token> rightTokens = tokenize(normalize(right));
		int length = Math.max(leftTokens.size(), rightTokens.size());

		for (int i = 0; i < length; i++) {
			Token leftToken = i < leftTokens.size() ? leftTokens.get(i) : Token.ZERO;
			Token rightToken = i < rightTokens.size() ? rightTokens.get(i) : Token.ZERO;
			int compared = leftToken.compareTo(rightToken);
			if (compared != 0) {
				return compared;
			}
		}

		return 0;
	}

	private static String normalize(String version) {
		if (version == null) {
			return "";
		}

		String normalized = version.trim().toLowerCase(Locale.ROOT);
		if (normalized.startsWith("v")) {
			return normalized.substring(1);
		}
		return normalized;
	}

	private static List<Token> tokenize(String version) {
		List<Token> tokens = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		Boolean numeric = null;

		for (int i = 0; i < version.length(); i++) {
			char character = version.charAt(i);
			if (!Character.isLetterOrDigit(character)) {
				appendToken(tokens, current, numeric);
				current.setLength(0);
				numeric = null;
				continue;
			}

			boolean characterIsNumeric = Character.isDigit(character);
			if (numeric != null && numeric != characterIsNumeric) {
				appendToken(tokens, current, numeric);
				current.setLength(0);
			}

			current.append(character);
			numeric = characterIsNumeric;
		}

		appendToken(tokens, current, numeric);
		return tokens;
	}

	private static void appendToken(List<Token> tokens, StringBuilder value, Boolean numeric) {
		if (value.isEmpty() || numeric == null) {
			return;
		}

		if (numeric) {
			tokens.add(new Token(new BigInteger(value.toString()), ""));
		} else {
			tokens.add(new Token(null, value.toString()));
		}
	}

	private record Token(BigInteger number, String text) implements Comparable<Token> {
		private static final Token ZERO = new Token(BigInteger.ZERO, "");

		@Override
		public int compareTo(Token other) {
			if (number != null && other.number != null) {
				return number.compareTo(other.number);
			}

			if (number != null) {
				return 1;
			}

			if (other.number != null) {
				return -1;
			}

			return text.compareTo(other.text);
		}
	}
}
