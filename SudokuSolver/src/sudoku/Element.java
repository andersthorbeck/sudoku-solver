package sudoku;

public class Element implements Comparable<Element> {
	
	public final int ordinal;
	public final String content;
	
	Element(int ordinal, String content) {
		this.ordinal = ordinal;
		this.content = content;
	}

	@Override
	public String toString() {
		return content;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ordinal;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Element))
			return false;
		Element other = (Element) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (ordinal != other.ordinal)
			return false;
		return true;
	}

	@Override
	public int compareTo(Element e) {
		return ordinal - e.ordinal;
	}
	
}
