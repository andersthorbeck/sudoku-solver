package sudoku;

import util.Coord;

public class Row extends Line implements Section {

	public Row(Grid grid, int index) {
		super(grid, index);
	}

	@Override
	protected Coord findIthCoord(int i) {
//		System.out.println("Row.findIthCoord, uses index. index="+index+", i="+i+", coord="+new Coord(index, i));
		return new Coord(index, i);
	}

	@Override
	public boolean contains(Coord coord) {
		return coord.row == index;
	}

	@Override
	public String toString() {
		return "Row [index=" + index + "]";
	}

}
