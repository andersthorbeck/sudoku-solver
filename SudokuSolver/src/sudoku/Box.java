package sudoku;

import util.Coord;
import util.Dims;

public class Box extends AbstractSection implements Section {

	Coord boxCoord;
	Coord topLeft;
	Dims dims;
	
	public Box(Grid grid, Coord boxCoord) {
		super(grid);
//		System.out.println("Box constructor, dims and topLeft about to be set to "+dims+", "+topLeft);
		this.boxCoord = boxCoord;
		this.dims = grid.getCellsPerBox();
		this.topLeft = boxCoord.multiply(dims);
		collateCells();
	}

	@Override
	protected Coord findIthCoord(int i) {
//		System.out.println("Column.findIthCoord, uses index. dims="+dims+", topLeft="+topLeft+", i="+i+", coord="+topLeft.plus(new Coord(i / dims.width, i % dims.width)));
		return topLeft.plus(new Coord(i / dims.width, i % dims.width));
	}

	@Override
	public boolean contains(Coord coord) {
		return topLeft.row <= coord.row && coord.row < topLeft.row + dims.height
			&& topLeft.col <= coord.col && coord.col < topLeft.col + dims.width;
	}

	@Override
	public String toString() {
		return "Box [boxCoord=" + boxCoord + "]";
	}

}
