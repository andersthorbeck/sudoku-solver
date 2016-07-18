package sudoku;

import util.Coord;

public class Diagonal extends AbstractSection implements Section {

	private boolean leadingDiagonal;
	private int indexSum;	// Only used for secondary diagonal, but doesn't do any harm with leading.
	
	public Diagonal(Grid grid, boolean leadingDiagonal) {
		super(grid);
		this.leadingDiagonal = leadingDiagonal;
		this.indexSum = grid.getNumElements() - 1;
		collateCells();
	}

	@Override
	protected Coord findIthCoord(int i) {
		return leadingDiagonal ? new Coord(i, i) : new Coord(i, indexSum - i);
	}

	@Override
	public boolean contains(Coord coord) {
		return leadingDiagonal ? coord.row == coord.col
							   : coord.row + coord.col == indexSum;
	}

	@Override
	public String toString() {
		return "Diagonal [leadingDiagonal=" + leadingDiagonal + "]";
	}
	
}
