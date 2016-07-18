package util;

/**
 * Dimensions
 * 
 * NOTE: By convention, width comes first, height comes second.
 */
public final class Dims {
	public final int width, height;
	
	public Dims(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public Dims flip() {
		return new Dims(height, width);
	}
	
	public int product() {
		return width * height;
	}
	
	public Dims multiply(Dims dims) {
		return new Dims(width * dims.width, height * dims.height);
	}

	public Dims divide(Dims dims) {
		return new Dims(width / dims.width, height / dims.height);
	}

	@Override
	public String toString() {
		return "Dims [width=" + width + ", height=" + height + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Dims))
			return false;
		Dims other = (Dims) obj;
		if (height != other.height)
			return false;
		if (width != other.width)
			return false;
		return true;
	}
}
