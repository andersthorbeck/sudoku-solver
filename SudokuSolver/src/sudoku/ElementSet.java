package sudoku;

import java.util.Arrays;
import java.util.Iterator;

public class ElementSet implements Iterable<Element> {
	
	private static final String EMPTY_ELEMENTS_ERROR = "All elements must be non-empty";
	private static final String DUPLICATE_ELEMENTS_ERROR = "There must be no identical elements";
	private static final String NON_POSITIVE_NUM_ELEMENTS_ERROR = "The number of elements to generate must be positive.";
	private static final String BAD_FIRST_LETTER_ERROR = "A set of alphabetic elements must start with a letter between 'a' and 'z', or 'A' and 'Z'.";
	private static final String TOO_MANY_ALPHABETIC_ELEMENTS_ERROR = "A set of alphabetic elements starting from '%s' cannot have more than %d elements.";
	public final String[] elementContents;
	final Element[] elements;
	final int numElements;
	public final int maxContentWidth;
	
	// Want to ensure that the ordering on the ordinals and on the contents matches.
	public ElementSet(String... elementStrings) throws CannotCreateElementSetException {
		this.elementContents = elementStrings;
		this.numElements = elementContents.length;
		
		if (existsEmptyContents()) {
			throw new CannotCreateElementSetException(EMPTY_ELEMENTS_ERROR);
		}
		Arrays.sort(elementContents);		// In place sorting
		if (existsDuplicates()) {
			throw new CannotCreateElementSetException(DUPLICATE_ELEMENTS_ERROR);
		}

		this.elements = new Element[numElements];
		for (int i = 0; i < numElements; i++) {
			elements[i] = new Element(i, elementContents[i]);
		}
		
		int maxLength = 0;
		for (String contents : elementContents) {
			maxLength = Math.max(maxLength, contents.length());
		}
		this.maxContentWidth = maxLength;
	}
	
	// Post-condition: Do not alter the array in any way (we don't want numElements to be incorrect)
	private boolean existsEmptyContents() {
		for (String contents : elementContents) {
			if (contents == null || contents.length() <= 0) {
				return true;
			}
		}
		return false;
	}
	
	// Pre-condition: elementContents is already sorted
	// Post-condition: Do not alter the array in any way (we don't want numElements to be incorrect)
	private boolean existsDuplicates() {
		for (int i = 1; i < numElements; i++) {
			if (elementContents[i].equals(elementContents[i - 1])) {
				return true;
			}
		}
		return false;
	}
	
	public Element getCorrespondingElement(String string) {
		int index = Arrays.binarySearch(elementContents, string);
		return index < 0 ? null : elements[index];
	}
	
	public Element getElement(int ordinal) {
		return elements[ordinal];
	}

	@Override
	public Iterator<Element> iterator() {
		return new ElementIterator();
	}
	
	private class ElementIterator implements Iterator<Element> {

		private int index = 0;
		
		@Override
		public boolean hasNext() {
			return index < numElements;
		}

		@Override
		public Element next() {
			return elements[index++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public static String[] generateNumberRange(int first, int length)
			throws CannotCreateElementSetException {
		if (length <= 0) {
			throw new CannotCreateElementSetException(NON_POSITIVE_NUM_ELEMENTS_ERROR);
		}
		String[] range = new String[length];
		for (int i = 0; i < length; i++) {
			range[i] = String.valueOf(first + i);
		}
		return range;
	}

	public static String[] generateAlphabeticRange(char first, int length)
			throws CannotCreateElementSetException {
		if (length <= 0) {
			throw new CannotCreateElementSetException(NON_POSITIVE_NUM_ELEMENTS_ERROR);
		}
		
		boolean lower = 'a' <= first && first <= 'z';
		boolean upper = 'A' <= first && first <= 'Z';
		if (!(lower || upper)) {
			throw new CannotCreateElementSetException(BAD_FIRST_LETTER_ERROR);
		}

		int maxValue = upper ? (int) 'Z' : (int) 'z';
		int maxNumElements = maxValue - ((int) first - 1);
		// Ensure length is not too long to overrun the a-z/A-Z alphabetic range
		if (length > maxNumElements) {
			throw new CannotCreateElementSetException(
					String.format(TOO_MANY_ALPHABETIC_ELEMENTS_ERROR, first, maxNumElements));
		}
		
		String[] range = new String[length];
		for (int i = 0; i < length; i++) {
			range[i] = String.valueOf((char)((int)first + i));
		}
		return range;
	}
	
	public static class CannotCreateElementSetException extends Exception {

		public CannotCreateElementSetException(String string) {
			super(string);
		}

		private static final long serialVersionUID = -6544129302663668859L;
		
	}

}
