package solver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import sudoku.Cell;
import sudoku.Element;
import sudoku.ElementSet;
import sudoku.ElementSet.CannotCreateElementSetException;
import sudoku.Grid;
import sudoku.Section;
import util.Coord;
import util.Dims;
import util.VerbosePrintStream;

@SuppressWarnings("unused")
public class SudokuSolver {
	
	public static final VerbosePrintStream out = new VerbosePrintStream(System.out);

	private final GridNode root;
	private final Grid original;
	
	private List<GridNode> solutions = new LinkedList<GridNode>();
	
	public SudokuSolver(Grid grid) {
		this.root = new GridNode(null, grid);
		this.original = new Grid(grid);
	}
	
	public Grid solve() {
		try {
			solve(root);
		} catch (NullPointerException e) {
			// In case I fucked something up.
			root.grid.printBoard();
			throw e;
		}
		
		System.out.println("Number of solutions: " + solutions.size());
		
		for (GridNode node : solutions) {
			System.out.println("\n\n\n\n");
			printGuesses(node);
		}
		
		return solutions.isEmpty() ? null : solutions.get(0).grid;
	}
	
	private void solve(GridNode node) {
		Grid grid = node.grid;
		try {
			while (grid.getNumUnfilled() > 0) {
				SudokuSolver.out.println("Unfilled left: " + grid.getNumUnfilled());
				solveOneCell(grid);
			}
		} catch (NeedToGuessException e) {
			Cell guessAt = findBestChoiceGuessableCell(grid);
			Coord guessCoord = guessAt.getCoord();
			node.children = new LinkedList<GridNode>();
			node.splitOn = guessCoord;
			
			// Create the child nodes
			for (Element option : guessAt.getAllPossible()) {
				Grid possibleGrid = new Grid(grid);
				possibleGrid.set(guessCoord, option);
				GridNode childNode = new GridNode(node, possibleGrid);
				node.children.add(childNode);
			}
			
			// Expand the child nodes
			for (GridNode child : node.children) {
				solve(child);
			}
			return;
			
		} catch (NoSolutionException e) {
			node.successful = false;
			SudokuSolver.out.println("Node " + node + " had no solution: " + e.getMessage() + "\n");
			return;
		}
		
		// At this point, all the cells in the grid are filled
		
		if (grid.isSolved()) {
			node.successful = true;
			solutions.add(node);
			GridNode ancestor = node.parent;
			while (ancestor != null) {
				ancestor.successfulChild = true;
				ancestor = ancestor.parent;
			}
			
			System.out.println("Board solved correctly! Final board state:");
			grid.printBoard();
		} else {
			node.successful = false;
			if (grid.verify())
				System.out.println("Board unsolved, but correctly filled");
			else
				System.out.println("Board incorrectly filled.");
		}
		System.out.println();
	}
	
	private void solveOneCell(Grid grid) throws NeedToGuessException, NoSolutionException {
		Cell filledCell;
		
		filledCell = fillSinglePossibility(grid);
		if (filledCell != null) {
			SudokuSolver.out.println("Filled cell " + filledCell + " from only possibility");
			return;
		}

		filledCell = byElimination(grid);
		if (filledCell != null) {
			SudokuSolver.out.println("Filled cell " + filledCell + " by elimination");
			return;
		}

		SudokuSolver.out.println("Couldn't fill a cell.");
		throw new NeedToGuessException();
	}
	
	private Cell fillSinglePossibility(Grid grid) {
		for (Cell cell : grid)
			if (!cell.isFilled()) {
				Element onlyPossibility = cell.getOnlyPossible();
				if (onlyPossibility != null) {
					grid.set(cell.getCoord(), onlyPossibility);
					return cell;
				}
			}
		return null;
	}
	
	// Find some section where an element has only 1 eligible cell.
	private Cell byElimination(Grid grid) throws NoSolutionException {
		for (List<? extends Section> sectionCollection : grid.allSections)
			for (Section section : sectionCollection) {
				if (section.isFull())
					continue;
				elementLoop:
				for (Element elem : grid.elements) {
					if (section.isFilledIn(elem))
						continue;
					Cell possibleCell = null;
					for (Cell cell : section) {
						if (cell.isPossible(elem)) {
							if (possibleCell == null)
								possibleCell = cell;
							else
								continue elementLoop;
						}
					}
					if (possibleCell == null)
						// The element fits nowhere in a section, the grid is unsolvable
						throw new NoSolutionException("Section: " + section + ", element: " + elem);
					grid.set(possibleCell.getCoord(), elem);
					return possibleCell;
				}
			}
		return null;
	}

	/**
	 * Find a cell which is a good choice to guess about.
	 * Criteria for cell choice:
	 * 	1.	Has as few possible element values as possible.
	 *  2.	The possible elements are rare in the grid currently.
	 *  3.	The sections containing the cell have as few unfilled cells as possible.
	 * @return
	 */
	private Cell findBestChoiceGuessableCell(Grid grid) {
		// Criterion 1
		
		// Map from an int to the collection of cells which have this many
		// different potential cell values currently.
		TreeMap<Integer, List<Cell>> optionsLists = new TreeMap<Integer, List<Cell>>();
		
		for (Cell cell : grid)
			if (!cell.isFilled()) {
				int numPossibilities = cell.getNumPossible();
				if (!optionsLists.containsKey(numPossibilities))
					optionsLists.put(numPossibilities, new ArrayList<Cell>());
				optionsLists.get(numPossibilities).add(cell);
			}
		
		List<Cell> minOptionsCells = optionsLists.firstEntry().getValue();
		
		// Criterion 2
		int[] elemFrequencies = grid.getElemFrequencies();
		TreeMap<Integer, List<Cell>> optionRarity = new TreeMap<Integer, List<Cell>>();

		for (Cell cell : minOptionsCells) {
			// Calculate the current rarity on the grid of the elements the cell
			// could have as value
			int raritySum = 0;
			for (Element elem : cell.getAllPossible())
				// Square the frequency, as we want lower frequencies to be
				// significantly more important.
				raritySum += java.lang.Math.pow(elemFrequencies[elem.ordinal], 2);
			
			if (!optionRarity.containsKey(raritySum))
				optionRarity.put(raritySum, new LinkedList<Cell>());
			optionRarity.get(raritySum).add(cell);
		}
		
		List<Cell> rarestOptions = optionRarity.firstEntry().getValue();
		
		// Criterion 3
		TreeMap<Integer, List<Cell>> sectionDesaturation = new TreeMap<Integer, List<Cell>>();
		
		for (Cell cell : rarestOptions) {
			List<Section> containingSections = grid.collateContainingSections(cell.getCoord());
			// This value technically denotes the "desaturation" of the section,
			// i.e. how many unfilled cells there are, rather than how many filled.
			int desaturationSum = 0;
			for (Section sec : containingSections)
				desaturationSum += sec.getNumUnfilled();
			
			if (!sectionDesaturation.containsKey(desaturationSum))
				sectionDesaturation.put(desaturationSum, new LinkedList<Cell>());
			sectionDesaturation.get(desaturationSum).add(cell);
		}
		
		// Least desaturated, i.e. with lowest desaturation sum, fewest unfilled cells.
		List<Cell> mostSaturated = sectionDesaturation.firstEntry().getValue();
		
		// Return the first cell in this narrowed down collection of cells.
		return mostSaturated.get(0);
	}
	
	private void printGuesses(GridNode successfulNode) {
		Stack<GridNode> nodeStack = new Stack<GridNode>();
		GridNode node = successfulNode;
		while (node != null) {
			nodeStack.push(node);
			node = node.parent;
		}
		System.out.println("Original grid:");
		original.printBoard();
		System.out.println();
		
		GridNode descendant = nodeStack.pop();
		while (descendant != successfulNode) {
			Coord guessCoord = descendant.splitOn;
			Element guessed = nodeStack.peek().grid.getCell(guessCoord).getValue();
			System.out.println("Board before guessing " + guessed + " at coordinate " + guessCoord + ":");
			descendant.grid.printBoard();
			System.out.println();
			
			descendant = nodeStack.pop();
		}
		
		System.out.println("Successful configuration of board:");
		successfulNode.grid.printBoard();
		System.out.println();
	}
	
	public static void main(String[] args) {
		Grid grid;
//		grid = createKnownGrid3x3AI_Escargot();
//		grid = createKnownGrid2x3();
//		grid = createKnownGrid3x3Blah();
		grid = createKnownGrid3x3_17PreFilled();
		System.out.println("Initial board state:");
		grid.printBoard();
		
		System.out.println("Creating solver");
		
		SudokuSolver solver = new SudokuSolver(grid);
		solver.solve();
	}
	
	private static String[] createElements6Numbers() {
//		Element[] elems = new Element[6];
//		Element e1 = new Element(0, "1"); elems[0] = e1;
//		Element e2 = new Element(1, "2"); elems[1] = e2;
//		Element e3 = new Element(2, "3"); elems[2] = e3;
//		Element e4 = new Element(3, "4"); elems[3] = e4;
//		Element e5 = new Element(4, "5"); elems[4] = e5;
//		Element e6 = new Element(5, "6"); elems[5] = e6;
//		return elems;

//		return new String[] {"1", "2", "3", "4", "5", "6"};
		try {
			return ElementSet.generateNumberRange(1, 6);
		} catch (CannotCreateElementSetException e) {
			// Should never happen.
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	private static String[] createElements9Numbers() {
//		Element[] elems = new Element[9];
//		Element e1 = new Element(0, "1"); elems[0] = e1;
//		Element e2 = new Element(1, "2"); elems[1] = e2;
//		Element e3 = new Element(2, "3"); elems[2] = e3;
//		Element e4 = new Element(3, "4"); elems[3] = e4;
//		Element e5 = new Element(4, "5"); elems[4] = e5;
//		Element e6 = new Element(5, "6"); elems[5] = e6;
//		Element e7 = new Element(6, "7"); elems[6] = e7;
//		Element e8 = new Element(7, "8"); elems[7] = e8;
//		Element e9 = new Element(8, "9"); elems[8] = e9;
//		return elems;

//		return new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
		try {
			return ElementSet.generateNumberRange(1, 9);
		} catch (CannotCreateElementSetException e) {
			// Should never happen
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	private static Grid createEmptyGrid(Dims cellsPerBox, String[] elemStrings) {
		try {
			return new Grid(cellsPerBox, elemStrings);
		} catch (CannotCreateElementSetException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	private static Grid createKnownGrid2x3() {
		
		String[] elemStrings = createElements6Numbers();
		
		Dims cellsPerBox = new Dims(3, 2);
		
		System.out.println("Creating a grid with box dimension 3 cells wide, 2 cells high, elements 1-6");
		Grid grid = createEmptyGrid(cellsPerBox, elemStrings);
		ElementSet elems = grid.elements;
		
		// Add elements
		grid.set(new Coord(0, 2), elems.getElement(2));
		grid.set(new Coord(0, 4), elems.getElement(4));
		grid.set(new Coord(0, 5), elems.getElement(0));
		grid.set(new Coord(2, 0), elems.getElement(2));
		grid.set(new Coord(2, 5), elems.getElement(3));
		grid.set(new Coord(3, 0), elems.getElement(3));
		grid.set(new Coord(3, 5), elems.getElement(1));
		grid.set(new Coord(5, 0), elems.getElement(1));
		grid.set(new Coord(5, 1), elems.getElement(3));
		grid.set(new Coord(5, 3), elems.getElement(5));
		
		return grid;
	}
	
	private static Grid createKnownGrid3x3() {
		
		String[] elemStrings = createElements9Numbers();
		
		Dims cellsPerBox = new Dims(3, 3);
		
		System.out.println("Creating a grid with box dimension 3 cells wide, 3 cells high, elements 1-9");
		Grid grid = createEmptyGrid(cellsPerBox, elemStrings);

		ElementSet elems = grid.elements;
		
		// Add elements
		grid.set(new Coord(0, 2), elems.getElement(1));
		grid.set(new Coord(0, 5), elems.getElement(5));
		grid.set(new Coord(1, 4), elems.getElement(7));
		grid.set(new Coord(1, 8), elems.getElement(8));
		grid.set(new Coord(2, 0), elems.getElement(8));
		grid.set(new Coord(2, 1), elems.getElement(3));
		grid.set(new Coord(2, 4), elems.getElement(0));
		grid.set(new Coord(2, 6), elems.getElement(7));
		grid.set(new Coord(3, 0), elems.getElement(6));
		grid.set(new Coord(3, 3), elems.getElement(5));
		grid.set(new Coord(3, 7), elems.getElement(1));
		grid.set(new Coord(4, 1), elems.getElement(2));
		grid.set(new Coord(4, 2), elems.getElement(4));
		grid.set(new Coord(4, 6), elems.getElement(8));
		grid.set(new Coord(4, 7), elems.getElement(5));
		grid.set(new Coord(5, 1), elems.getElement(5));
		grid.set(new Coord(5, 8), elems.getElement(0));
		grid.set(new Coord(6, 2), elems.getElement(5));
		grid.set(new Coord(6, 4), elems.getElement(2));
		grid.set(new Coord(6, 7), elems.getElement(4));
		grid.set(new Coord(6, 8), elems.getElement(1));
		grid.set(new Coord(7, 0), elems.getElement(2));
		grid.set(new Coord(7, 4), elems.getElement(8));
		grid.set(new Coord(7, 7), elems.getElement(0));
		grid.set(new Coord(8, 3), elems.getElement(3));
		grid.set(new Coord(8, 6), elems.getElement(2));
		
		return grid;
	}
	
	private static Grid createKnownGrid3x3VeryHard() {
		
		String[] elemStrings = createElements9Numbers();
		
		Dims cellsPerBox = new Dims(3, 3);
		
		System.out.println("Creating a grid with box dimension 3 cells wide, 3 cells high, elements 1-9");
		Grid grid = createEmptyGrid(cellsPerBox, elemStrings);
		ElementSet elems = grid.elements;
		
		// Add elements
		grid.set(new Coord(0, 0), elems.getElement(7));
		grid.set(new Coord(0, 1), elems.getElement(5));
		grid.set(new Coord(0, 4), elems.getElement(1));
		grid.set(new Coord(1, 3), elems.getElement(6));
		grid.set(new Coord(1, 7), elems.getElement(4));
		grid.set(new Coord(1, 8), elems.getElement(8));
		grid.set(new Coord(3, 4), elems.getElement(5));
		grid.set(new Coord(3, 6), elems.getElement(7));
		grid.set(new Coord(4, 1), elems.getElement(3));
		grid.set(new Coord(5, 2), elems.getElement(4));
		grid.set(new Coord(5, 3), elems.getElement(2));
		grid.set(new Coord(5, 8), elems.getElement(6));
		grid.set(new Coord(7, 1), elems.getElement(1));
		grid.set(new Coord(7, 6), elems.getElement(5));
		grid.set(new Coord(8, 2), elems.getElement(6));
		grid.set(new Coord(8, 3), elems.getElement(4));
		grid.set(new Coord(8, 5), elems.getElement(8));

//		grid.set(new Coord(5, 5), elems[3/*0*/]);	// Guess
		
		return grid;
	}
	
	private static Grid createKnownGrid3x3Blah() {
		
		String[] elemStrings = createElements9Numbers();
		
		Dims cellsPerBox = new Dims(3, 3);
		
		System.out.println("Creating a grid with box dimension 3 cells wide, 3 cells high, elements 1-9");
		Grid grid = createEmptyGrid(cellsPerBox, elemStrings);
		ElementSet elems = grid.elements;
		
		// Add elements
		grid.set(new Coord(0, 2), elems.getElement(3));
		grid.set(new Coord(0, 4), elems.getElement(8));
		grid.set(new Coord(0, 7), elems.getElement(0));
		grid.set(new Coord(1, 0), elems.getElement(8));
		grid.set(new Coord(1, 5), elems.getElement(0));
		grid.set(new Coord(2, 0), elems.getElement(0));
		grid.set(new Coord(2, 1), elems.getElement(2));
		grid.set(new Coord(2, 2), elems.getElement(1));
		grid.set(new Coord(2, 3), elems.getElement(4));
		grid.set(new Coord(2, 6), elems.getElement(7));
		grid.set(new Coord(3, 5), elems.getElement(4));
		grid.set(new Coord(4, 1), elems.getElement(8));
		grid.set(new Coord(4, 2), elems.getElement(4));
		grid.set(new Coord(4, 4), elems.getElement(7));
		grid.set(new Coord(4, 6), elems.getElement(2));
		grid.set(new Coord(4, 7), elems.getElement(6));
		grid.set(new Coord(5, 3), elems.getElement(5));
		grid.set(new Coord(6, 2), elems.getElement(6));
		grid.set(new Coord(6, 5), elems.getElement(7));
		grid.set(new Coord(6, 6), elems.getElement(4));
		grid.set(new Coord(6, 7), elems.getElement(5));
		grid.set(new Coord(6, 8), elems.getElement(1));
		grid.set(new Coord(7, 3), elems.getElement(6));
		grid.set(new Coord(7, 8), elems.getElement(8));
		grid.set(new Coord(8, 1), elems.getElement(7));
		grid.set(new Coord(8, 4), elems.getElement(3));
		grid.set(new Coord(8, 6), elems.getElement(0));
		
		return grid;
	}
	
	private static Grid createKnownGrid3x3AI_Escargot() {
		
		String[] elemStrings = createElements9Numbers();
		
		Dims cellsPerBox = new Dims(3, 3);
		
		System.out.println("Creating a grid with box dimension 3 cells wide, 3 cells high, elements 1-9");
		Grid grid = createEmptyGrid(cellsPerBox, elemStrings);
		ElementSet elems = grid.elements;
		
		// Add elements
		grid.set(new Coord(0, 0), elems.getElement(0));
		grid.set(new Coord(0, 5), elems.getElement(6));
		grid.set(new Coord(0, 7), elems.getElement(8));
		grid.set(new Coord(1, 1), elems.getElement(2));
		grid.set(new Coord(1, 4), elems.getElement(1));
		grid.set(new Coord(1, 8), elems.getElement(7));
		grid.set(new Coord(2, 2), elems.getElement(8));
		grid.set(new Coord(2, 3), elems.getElement(5));
		grid.set(new Coord(2, 6), elems.getElement(4));
		grid.set(new Coord(3, 2), elems.getElement(4));
		grid.set(new Coord(3, 3), elems.getElement(2));
		grid.set(new Coord(3, 6), elems.getElement(8));
		grid.set(new Coord(4, 1), elems.getElement(0));
		grid.set(new Coord(4, 4), elems.getElement(7));
		grid.set(new Coord(4, 8), elems.getElement(1));
		grid.set(new Coord(5, 0), elems.getElement(5));
		grid.set(new Coord(5, 5), elems.getElement(3));
		grid.set(new Coord(6, 0), elems.getElement(2));
		grid.set(new Coord(6, 7), elems.getElement(0));
		grid.set(new Coord(7, 1), elems.getElement(3));
		grid.set(new Coord(7, 8), elems.getElement(6));
		grid.set(new Coord(8, 2), elems.getElement(6));
		grid.set(new Coord(8, 6), elems.getElement(2));
		
		return grid;
	}

	private static Grid createKnownGrid3x3_17PreFilled() {
		
		String[] elemStrings = createElements9Numbers();
		
		Dims cellsPerBox = new Dims(3, 3);
		
		System.out.println("Creating a grid with box dimension 3 cells wide, 3 cells high, elements 1-9");
		Grid grid = createEmptyGrid(cellsPerBox, elemStrings);
		ElementSet elems = grid.elements;
		
		// Add elements
		grid.set(new Coord(0, 7), elems.getElement(0));
		grid.set(new Coord(0, 8), elems.getElement(1));
		grid.set(new Coord(1, 4), elems.getElement(2));
		grid.set(new Coord(1, 5), elems.getElement(4));
		grid.set(new Coord(2, 3), elems.getElement(5));
		grid.set(new Coord(2, 7), elems.getElement(6));
		grid.set(new Coord(3, 0), elems.getElement(6));
		grid.set(new Coord(3, 6), elems.getElement(2));
		grid.set(new Coord(4, 3), elems.getElement(3));
		grid.set(new Coord(4, 6), elems.getElement(7));
		grid.set(new Coord(5, 0), elems.getElement(0));
		grid.set(new Coord(6, 3), elems.getElement(0));
		grid.set(new Coord(6, 4), elems.getElement(1));
		grid.set(new Coord(7, 1), elems.getElement(7));
		grid.set(new Coord(7, 7), elems.getElement(3));
		grid.set(new Coord(8, 1), elems.getElement(4));
		grid.set(new Coord(8, 6), elems.getElement(5));

		return grid;
	}

	/**
	 * Primarily needed for branching grid structures, where a guess must be
	 * made as to what element goes in a particular cell.
	 * For such a guess, the node representing the current state of the grid has
	 * as many children as there are guesses. This only applies for branching
	 * factors greater than one; any choices that can be made without guessing
	 * do not necessitate the creation of more nodes. 
	 * @author Anders
	 *
	 */
	private class GridNode {
		private final Grid grid;
		private GridNode parent;
		private List<GridNode> children;
		// Only set once the node gets children
		private Coord splitOn = null;
		// private Stack<Action> moves;
		private Boolean successful = null;
		private boolean successfulChild = false;
		GridNode(GridNode parent, Grid grid) {
			this.parent = parent;
			this.grid = grid;
		}
				
	}
	
}
