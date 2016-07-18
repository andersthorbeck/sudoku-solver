package sudoku;

import util.Coord;
import util.Dims;

public class SizeInfo {

	/**
	 * The number of {@link Cell}s wide and high a {@link Box} is.
	 * All boxes in a grid have the same dimensions.
	 */
	public final Dims cellsPerBox;
	/**
	 * The number of {@link Box}es wide and high a Grid is. This is the flipped
	 * dimension of {@link #cellsPerBox}.
	 */
	public final Dims boxesPerGrid;
	/**
	 * The number of {@link Cell}s wide and high a grid is.
	 * The width and the height will always be the same number.
	 */
	public final Dims cellsPerGrid;


	SizeInfo(Dims cellsPerBox) {
		this.cellsPerBox = cellsPerBox;
		this.boxesPerGrid = cellsPerBox.flip();
		this.cellsPerGrid = this.boxesPerGrid.multiply(this.cellsPerBox);
	}
	
	
	Dims getCellsPerBox() {
		return cellsPerBox;
	}
	
	Dims getBoxesPerGrid() {
		return boxesPerGrid;
	}
	
	Dims getCellsPerGrid() {
		return cellsPerGrid;
	}
	

	public Coord boxCoordToCellCoord(Coord boxCoord) {
		return boxCoord.multiply(cellsPerBox);		
	}

	public Coord cellCoordToBoxCoord(Coord cellCoord) {
		return cellCoord.divide(cellsPerBox);		
	}
	
	public Coord boxIndexToBoxCoord(int i) {
		int numBoxesWide = boxesPerGrid.width;
		return new Coord(i / numBoxesWide, i % numBoxesWide);
	}
	
	public int boxCoordToBoxIndex(Coord boxCoord) {
		return boxCoord.row * boxesPerGrid.width + boxCoord.col;
	}
	
	public int cellCoordToBoxIndex(Coord cellCoord) {
		return boxCoordToBoxIndex(cellCoordToBoxCoord(cellCoord));
	}
	
	public Coord cellIndexToCellCoord(int cellIndex) {
		int numCellsWide = cellsPerGrid.width;
		return new Coord(cellIndex / numCellsWide, cellIndex % numCellsWide);
	}
	
	public int cellCoordToCellIndex(Coord cellCoord) {
		int numCellsWide = cellsPerGrid.width;
		return cellCoord.row * numCellsWide + cellCoord.col;
	}

}
