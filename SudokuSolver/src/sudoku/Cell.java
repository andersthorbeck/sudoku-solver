package sudoku;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import util.Coord;

public class Cell {
	public final Coord coord;
	private final Grid grid;
	private boolean[] possible;
	private Element value = null;
	
	Cell(Grid grid, Coord coord) {
		this.grid = grid;
		this.coord = coord;
		this.possible = new boolean[grid.getNumElements()];
//		for (int i = 0; i < possible.length; i++)
//			possible[i] = true;
		Arrays.fill(possible, true);
	}
	
	Cell(Grid grid, Coord coord, Element value) {
//		this.grid = grid;
//		this.coord = coord;
//		this.value = value;
//		this.possible = new boolean[grid.getNumElements()];
//		for (int i = 0; i < possible.length; i++)
//			possible[i] = value == null;
		this(grid, coord);
		this.setValue(value);
	}
	
	void setValue(Element value) {
		this.value = value;
		if (value != null) {
			// No other value can be set here.
//			for (int i = 0; i < possible.length; i++)
//				possible[i] = false;
			Arrays.fill(possible, false);
		}
	}
	
	void setNotPossible(Element value) {
		possible[value.ordinal] = false;
	}
	
	public Element getValue() {
		return value;
	}
	
	public Coord getCoord() {
		return coord;
	}

	public boolean isFilled() {
		return value != null;
	}
	
	public boolean isPossible(Element element) {
		// This method relies on all the possible arrays of the grid being
		// properly updated every time a cell is filled.
		return element == null || possible[element.ordinal];
	}
	
	void resetPossibilities() {
		for (int i = 0; i < possible.length; i++)
			possible[i] = true;
	}
	
	public int getNumPossible() {
		int numPossible = 0;
		for (boolean possibility : possible)
			if (possibility)
				numPossible++;
		return numPossible;
	}
	
	// Returns the only possible element if there is only one, null otherwise
	public Element getOnlyPossible() {
		List<Element> allPossible = getAllPossible();
		return allPossible.size() == 1 ? allPossible.get(0) : null;
	}
	
	public List<Element> getAllPossible() {
		List<Element> allPossible = new LinkedList<Element>();
		for (Element e : grid.elements)
			if (possible[e.ordinal])
				allPossible.add(e);
		return allPossible;
	}
	
	@Override
	public String toString() {
		return "Cell [coord=" + coord + ", value=" + value + "]";
	}
	
}
