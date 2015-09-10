package application.service;

public class PoundedElement<T> implements Comparable<PoundedElement<T>> {

	private final int pound;

	public final T data;

	public PoundedElement(int pound, T data) {
		super();
		this.pound = pound;
		this.data = data;
	}

	@Override
	public int compareTo(PoundedElement<T> o) {
		return o.pound - this.pound;
	}
}
