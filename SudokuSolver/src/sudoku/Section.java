package sudoku;

import util.Coord;

public interface Section extends Iterable<Cell> {

	Grid getGrid();
	
	Iterable<Cell> getCells();
	
	boolean isFilledIn(Element element);
	Coord getCoordOf(Element element);
	Cell getCellAt(Coord coord);
	
	boolean verify();
	
	boolean contains(Coord coord);
	
	void setFilledIn(Element element, Cell cell);
	void resetFilledCount();
	
	boolean isFull();
	int getNumUnfilled();
	
}
