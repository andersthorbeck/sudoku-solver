package sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import util.Coord;

public abstract class AbstractSection implements Section {
	protected Grid grid;
	protected List<Cell> cells;
	// Which elements are currently filled in in the section
	protected boolean[] filled;
	
	AbstractSection(Grid grid) {
		this.grid = grid;
		int numElements = grid.getNumElements();
		this.cells = new ArrayList<Cell>(numElements);
		this.filled = new boolean[numElements];		// Defaults to false
		Arrays.fill(filled, false);
//		collateCells();	//WARNING: CALL THIS IN SUBCLASS, NOT HERE!
	}
	
	public Grid getGrid() {
		return grid;
	}
	
	public Iterable<Cell> getCells() {
		return cells;
	}

	@Override
	public Iterator<Cell> iterator() {
		return cells.iterator();
	}

	@Override
	public boolean isFilledIn(Element element) {
		return filled[element.ordinal];
//		return getCoordOf(element) != null;
	}
	
	@Override
	public int getNumUnfilled() {
		int unfilled = 0;
		for (boolean isFilled : filled)
			if (!isFilled)
				unfilled++;
		return unfilled;
	}
	
	@Override
	public boolean isFull() {
		return getNumUnfilled() == 0;
	}
	
	// TODO: Remove the element argument from this method?
	@Override
	public void setFilledIn(Element element, Cell filledCell) {
		filled[element.ordinal] = true;
		for (Cell c : this)
			if (c != filledCell)
				c.setNotPossible(element);
	}
	
	@Override
	public void resetFilledCount() {
		Arrays.fill(filled, false);
//		for (int i = 0; i < filled.length; i++)
//			filled[i] = false;
	}

	@Override
	public Coord getCoordOf(Element element) {
		for (Cell cell : this) {
			if (cell.getValue().equals(element))
				return cell.getCoord();
		}
		return null;
	}

	@Override
	public Cell getCellAt(Coord coord) {
		for (Cell cell : this) {
			if (cell.getCoord().equals(coord))
				return cell;
		}
//		assert(false); // Inputted Coord is not a part of this section.
		return null;
	}

	@Override
	public boolean verify() {
		for (int i = 0; i < filled.length; i++)
			filled[i] = false;
		
		boolean valid = true;
		for (Cell cell : this) {
			Element elem = cell.getValue();
			if (elem != null) {
				// If this element has already been filled in somewhere else
				if (filled[elem.ordinal])
					valid = false;
				filled[elem.ordinal] = true;
			}
		}
		return valid;
	}
	
	/**
	 * NOTE: Don't call this method in AbstractSection, call it in the
	 * constructor of the implemented section, as it may rely on fields of that
	 * selection.
	 */
	protected void collateCells() {
		cells.clear();	// In case you done goof'd and call this more than once
		int size = grid.getNumElements();
		for (int i = 0; i < size; i++) {
			cells.add(grid.getCell(findIthCoord(i)));
		}
	}
	
	protected abstract Coord findIthCoord(int i);


}
