package sudoku;


// Subclassed by row and column, but not by diagonal.
public abstract class Line extends AbstractSection {

	protected int index;

	public Line(Grid grid, int index) {
		super(grid);
//		System.out.println("Line constructor, index about to be set to "+index);
		this.index = index;
		collateCells();
	}

}