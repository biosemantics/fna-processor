package edu.arizona.biosemantics.fnaprocessor.action.parenthesis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * BracketValidator validates weakly that opened brackets are closes appropriately.
 * Weakly in the sense that the order in which brackets are closed does not matter.
 * I.e. [(...text]) is a valid pattern just like [(...text...)] is
 */
public class BracketValidator {

	private Map<Character, Character> brackets = new LinkedHashMap<Character, Character>();
	private Set<Character> openingBrackets = new HashSet<Character>();
	private Set<Character> closingBrackets = new HashSet<Character>();

	public BracketValidator() {
		openingBrackets.add('(');
		openingBrackets.add('[');
		openingBrackets.add('{');

		closingBrackets.add(')');
		closingBrackets.add(']');
		closingBrackets.add('}');

		brackets.put('(', ')');
		brackets.put(')', '(');
		brackets.put('[', ']');
		brackets.put(']', '[');
		brackets.put('{', '}');
		brackets.put('}', '{');
	}

	/**
	 * Returns a map with the bracket count differences
	 * e.g. if text is "... (...[...]...." the map will contain
	 * ( = -1
	 * ) = 1
	 * [ = 0
	 * ] = 0
	 * @param text
	 * @return the bracket count differences
	 */
	public Map<Character, Integer> getBracketCountDifferences(String text) {
		Map<Character, Integer> result = new LinkedHashMap<Character, Integer>();
		for(Character character : brackets.keySet()) {
			result.put(character, 0);
		}

		Map<Character, Integer> openingBracketCounts = new HashMap<Character, Integer>();
		for(Character character : openingBrackets) {
			openingBracketCounts.put(character, 0);
		}

		for(int i=0; i<text.length(); i++) {
			char currentCharacter = text.charAt(i);
			if(openingBracketCounts.containsKey(currentCharacter)) {
				openingBracketCounts.put(currentCharacter, openingBracketCounts.get(currentCharacter) + 1);
			}
			if(closingBrackets.contains(currentCharacter)) {
				Character theOpeningBracket = brackets.get(currentCharacter);
				if(openingBracketCounts.get(theOpeningBracket) == 0)
					result.put(theOpeningBracket, result.get(theOpeningBracket) + 1);
				else
					openingBracketCounts.put(theOpeningBracket, openingBracketCounts.get(theOpeningBracket) - 1);
			}
		}

		for(Character character : openingBracketCounts.keySet()) {
			int count = openingBracketCounts.get(character);
			Character theClosingBracket = brackets.get(character);
			result.put(theClosingBracket, count);
		}

		return result;
	}

	/**
	 * Returns if the text is valid, i.e. if it does not contains any unmatched brackets.
	 * @param text
	 * @return if the text does not contain unmatched brackets.
	 */
	public boolean validate(String text) {
		Map<Character, Integer> openingBracketCounts = new HashMap<Character, Integer>();
		for(Character character : openingBrackets) {
			openingBracketCounts.put(character, 0);
		}

		for(int i=0; i<text.length(); i++) {
			char currentCharacter = text.charAt(i);
			if(openingBracketCounts.containsKey(currentCharacter)) {
				openingBracketCounts.put(currentCharacter, openingBracketCounts.get(currentCharacter) + 1);
			}
			if(closingBrackets.contains(currentCharacter)) {
				Character theOpeningBracket = brackets.get(currentCharacter);
				if(openingBracketCounts.get(theOpeningBracket) == 0)
					return false;
				else
					openingBracketCounts.put(theOpeningBracket, openingBracketCounts.get(theOpeningBracket) - 1);
			}
		}

		for(Character character : openingBracketCounts.keySet()) {
			int count = openingBracketCounts.get(character);
			if(count > 0)
				return false;
		}

		return true;
	}

}
