package sudoku;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import sudoku.ElementSet.CannotCreateElementSetException;
import util.Coord;
import util.Dims;


// Origin of grid coords is top left
/**
 * The full grid of {@link Cell}s that make up a Sudoku puzzle.
 * Contains all the Cells in a double array, but also stores the conceptual
 * {@link Section}s of the grid, i.e. {@link Row}s, {@link Column}s and
 * {@link Box}es, as well as possible custom ones.
 */
public class Grid implements Iterable<Cell> {
	
	//MAYBE: Extend to 3D? Take bixDims instead, an array of size 3
	
	public static final int DEFAULT_ROWS_PER_BOX = 3;
	public static final int DEFAULT_COLS_PER_BOX = 3;
	
	public final SizeInfo dimensions;
	
	/**
	 * The complete set of the Grid's unique {@link Element}s.
	 * Each {@link Section} of the solved Grid must contain exactly one copy of each
	 * element.
	 * The number of elements is equal to the size of any Section (e.g. a
	 * {@link Row}).
	 */
	public final ElementSet elements;
	// MAYBE: Remove Grid.numElements, since it can be accessed through grid.elements.numElements
	/**
	 * The number of unique {@link Element}s corresponding to the Grid layout.
	 * See {@link #elements}. 
	 */
	public final int numElements;
	/**
	 * The number of {@link Cell}s in a Grid which are currently unfilled, i.e.
	 * which do not have an {@link Element} assigned yet.
	 */
	private int unfilled;
	
	/**
	 * Grid of {@link Cell}s, height by width.
	 */
	private Cell[][] grid;
	
	/**
	 * All the {@link Row}s of this Grid.
	 */
	private List<Row> rows;
	/**
	 * All the {@link Column}s of this Grid.
	 */
	private List<Column> columns;
	/**
	 * All the {@link Box}es of this Grid.
	 */
	private List<Box> boxes;
	/**
	 * All the {@link Section}s of this Grid which are not {@link Row}s,
	 * {@link Column}s or {@link Box}es.
	 */
	private List<Section> otherSections;
	/**
	 * A meta-list of {@link #rows}, {@link #columns}, {@link #boxes} and
	 * {@link #otherSections}.
	 */
	public List<List<? extends Section>> allSections;

	/**
	 * Create an empty Grid.
	 * @param cellsPerBox The dimensions of {@link Cell}s in a {@link Box} of this Grid.
	 * @param elements The allowed {@link Element}s for this Grid.
	 *  There must be exactly as many elements as the product of cellsPerBox.
	 * @throws CannotCreateElementSetException 
	 */
	public Grid(Dims cellsPerBox, String[] elements) throws CannotCreateElementSetException {
		this(null, cellsPerBox, new ElementSet(elements));
	}
	
	/**
	 * Used for copying a grid.
	 */
	public Grid(Grid origGrid) {
		this(origGrid.grid, origGrid.dimensions.cellsPerBox, origGrid.elements);
	}
	
	private Grid(Cell[][] origGrid, Dims cellsPerBox, ElementSet elements) {
		assert(cellsPerBox.width > 0 && cellsPerBox.height > 0);
		assert(elements.numElements == cellsPerBox.width * cellsPerBox.height);
		this.dimensions = new SizeInfo(cellsPerBox);
		this.elements = elements;
		this.numElements = elements.numElements;
		
		// The width and height of the Grid in terms of Cells
		int width = dimensions.cellsPerGrid.width;
		int height = dimensions.cellsPerGrid.height;
		assert(origGrid == null || origGrid.length == height && origGrid[0].length == width);
		this.unfilled = this.dimensions.cellsPerGrid.product();

		// Instantiate cells
		this.grid = new Cell[height][width];
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				Cell cell = new Cell(this, new Coord(r, c));
				this.grid[r][c] = cell;
			}
		}
		
		// Instantiate sections
		rows = new ArrayList<Row>(numElements);
		columns = new ArrayList<Column>(numElements);
		boxes = new ArrayList<Box>(numElements);
		int numBoxesWide = dimensions.boxesPerGrid.width;
		for (int i = 0; i < numElements; i++) {
			rows.add(new Row(this, i));
			columns.add(new Column(this, i));
			boxes.add(new Box(this, new Coord(i / numBoxesWide, i % numBoxesWide)));
		}
		otherSections = new LinkedList<Section>();	// E.g. diagonals etc
		
		allSections = new LinkedList<List<? extends Section>>();
		allSections.add(rows);
		allSections.add(columns);
		allSections.add(boxes);
		allSections.add(otherSections);
		
		// Fill in the cells from the original grid (if there is one) only
		// after everything else, so stuff validates and updates correctly.
		if (origGrid != null)
			for (Cell cell : this) {
				Coord coord = cell.coord;
				Element value = origGrid[coord.row][coord.col].getValue();
				if (value != null) {
					this.set(coord, value);
				}
			}
	}
	
	public static SizeInfo getDefaultDimensions() {
		return new SizeInfo(new Dims(DEFAULT_COLS_PER_BOX, DEFAULT_ROWS_PER_BOX));
	}
	
	/**
	 * @return true iff the Grid is solved.
	 */
	public boolean isSolved() {
		// First check if the grid is consistent, i.e. that no section contains
		// two copies of the same element.
		if (!verify())
			return false;
		// If the grid is consistent and all cells are filled, then it is solved.
		for (List<? extends Section> sections : allSections)
			for (Section section : sections)
				if (!section.isFull())
					return false;
		return true;
	}
	
	/**
	 * Verifies whether the Grid is consistent.
	 * The Grid is consistent if no {@link Section} contains two copies of the same element.
	 * @return true iff the Grid is consistent.
	 */
	public boolean verify() {
		for (List<? extends Section> sectionList : allSections)
			for (Section section : sectionList)
				if (!section.verify())
					return false;
		return true;
	}
	
	/**
	 * Verifies the {@link Section}s containing the given {@link Cell}.
	 * @param coord The coordinate of the Cell in question.
	 * @return true iff the Cell is consistent with its containing sections.
	 */
	boolean verify(Coord coord) {
		List<Section> containingSections = collateContainingSections(coord);
		for (Section section : containingSections)
			if (!section.verify())
				return false;
		return true;
	}
	
	/**
	 * Check whether the given {@link Element} is currently an eligible option
	 * at the given {@link Cell}, i.e. if we cannot with certainty conclude that
	 * it is an ineligible option.
	 * @param coord The coordinate of the Cell in question.
	 * @param possibility The Element in question.
	 * @return true iff the given element is currently an eligible option at the
	 * given coordinate.
	 */
	boolean verify(Coord coord, Element possibility) {
		return getCell(coord).isPossible(possibility);
	}
	
	/*
	// Check there is nothing wrong with model (don't care about actual cell values)
	boolean checkConformity() {
		if (!allElementsUnique())
			return false;
		for (Cell cell : this)
			if (!isAnElement(cell.getValue()))
				return false;
		return true;
	}
	
	// Checks whether the provided element is one of this grid's elements.
	boolean isAnElement(Element element) {
		if (element == null)
			return true;
		for (Element e : elements)
			if (e == element)
				return true;
		return false;
	}
	
	boolean allElementsUnique() {
		for (int i = 0; i < numElements; i++) {
			if (elements.getElement(i) == null)
				return false;
			for (int j = i + 1; j < numElements; j++)
				if (elements.getElement(i) == elements.getElement(j))
					return false;
		}
		return true;
	}
	*/
	
	// Assume the cell's boolean possible array is valid
	public boolean set(Coord coord, Element possibility) {
		boolean allowed = verify(coord, possibility);
		assert (allowed);
		if (allowed) {
			Cell cell = getCell(coord); 
			Element prevValue = cell.getValue();
			// Set the actual value in the cell
			cell.setValue(possibility);
			unfilled += possibility != null ? -1 : (prevValue != null ? 1 : 0);
			if (possibility != null) {
				// Update all other cell in same section to no longer have this possibility
				List<Section> containingSections = collateContainingSections(coord);
				for (Section section : containingSections)
					section.setFilledIn(possibility, cell);
			} else {
				recalculatePossibilites();
			}
		}
		
		return allowed;
	}
	
	void recalculatePossibilites() {
		for (Cell cell : this) {
			cell.resetPossibilities();
		}
		for (List<? extends Section> sectionCollection : allSections) {
			for (Section section : sectionCollection) {
				section.resetFilledCount();
			}
		}
		
		for (Cell cell : this) {
			if (cell.isFilled()) {
				Element cellValue = cell.getValue();
				List<Section> containingSections = collateContainingSections(cell.getCoord());
				for (Section section : containingSections)
					section.setFilledIn(cellValue, cell);
			}
		}
	}
	
	@Override
	public Iterator<Cell> iterator() {
		return new GridIterator();
	}
	
	private class GridIterator implements Iterator<Cell> {

		int currIndex;
		final int totalCells = dimensions.cellsPerGrid.product();
		
		GridIterator() {
			currIndex = 0;
		}
		
		@Override
		public boolean hasNext() {
			return currIndex < totalCells;
		}

		@Override
		public Cell next() {
			return getCell(dimensions.cellIndexToCellCoord(currIndex++));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

	// Getters

	public int getNumUnfilled() {
		return unfilled;
	}
	
	public Cell getCell(Coord coord) {
		return getCell(coord.row, coord.col);
	}
	
	public Cell getCell(int row, int col) {
		return grid[row][col];
	}
	
	public ElementSet getElements() {
		return elements;
	}
	
	public int getNumElements() {
		return numElements;
	}
	
	Dims getCellsPerBox() {
		return dimensions.cellsPerBox;
	}
	
	Dims getBoxesPerGrid() {
		return dimensions.boxesPerGrid;
	}
	
	Dims getCellsPerGrid() {
		return dimensions.cellsPerGrid;
	}
	
	// Helper methods / Converters
	
	public int[] getElemFrequencies() {
		int[] elemFrequencies = new int[numElements];
		for (Cell cell : this) {
			Element e = cell.getValue();
			if (e != null)
				elemFrequencies[e.ordinal]++;
		}
		return elemFrequencies;
	}
	
	public List<Section> collateContainingSections(Coord coord) {
		List<Section> sections = new LinkedList<Section>();
		sections.add(rows.get(coord.row));
		sections.add(columns.get(coord.col));
		sections.add(boxes.get(dimensions.cellCoordToBoxIndex(coord)));
		for (Section section : otherSections)
			if (section.contains(coord))
				sections.add(section);
		return sections;
	}
	
	String boardToString() {
		int maxElemWidth = elements.maxContentWidth;
//		for (Element e : elements)
//			maxElemWidth = Math.max(maxElemWidth, e.content.length());
		String singleElemPlaceHolder = "";
		for (int i = 0; i < maxElemWidth; i++)
			singleElemPlaceHolder += "-";
		String horizontalDivider = "+";
		for (int i = 0; i < numElements; i++)
			horizontalDivider += singleElemPlaceHolder + "+";
		horizontalDivider += "\n";
		
		String board = horizontalDivider;
		for (Section row : rows) {
			board += "|";
			for (Cell cell : row) {
				Element elem = cell.getValue();
				int fillerSpace = elem == null ? maxElemWidth
						: maxElemWidth - elem.content.length();
				String spaces = "";
				for (int i = 0; i < fillerSpace; i++)
					spaces += " ";
				board += spaces + (elem == null ? "" : elem.content) + "|";
			}
			board += "\n" + horizontalDivider;
		}
		return board;
	}
	
	public void printBoard() {
		System.out.println(boardToString());
	}

}
