package sudoku;

import util.Coord;

public class Column extends Line implements Section {

	public Column(Grid grid, int index) {
		super(grid, index);
	}

	@Override
	protected Coord findIthCoord(int i) {
//		System.out.println("Column.findIthCoord, uses index. index="+index+", i="+i+", coord="+new Coord(i, index));
		return new Coord(i, index);
	}

	@Override
	public boolean contains(Coord coord) {
		return coord.col == index;
	}

	@Override
	public String toString() {
		return "Column [index=" + index + "]";
	}

}
