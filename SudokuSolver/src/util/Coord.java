package util;

/**
 * Row & Column Coordinate
 *
 * NOTE: By convention, row comes first, column comes second.
 */
public final class Coord {
	public final int row, col;
	
	public Coord(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public Coord plus(Coord other) {
		return new Coord(row + other.row, col + other.col);
	}
	
	public Coord multiply(Dims dims) {
		return new Coord(row * dims.height, col * dims.width);
	}

	public Coord divide(Dims dims) {
		return new Coord(row / dims.height, col / dims.width);
	}

	@Override
	public String toString() {
		return "Coord [row=" + row + ", col=" + col + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Coord))
			return false;
		Coord other = (Coord) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}
}
