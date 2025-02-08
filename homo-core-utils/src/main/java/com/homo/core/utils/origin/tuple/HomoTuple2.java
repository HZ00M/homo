
package com.homo.core.utils.origin.tuple;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
@Builder
public class HomoTuple2<T0, T1> extends HomoTuple {

	private static final long serialVersionUID = 1L;

	/** Field 0 of the tuple. */
	public T0 f0;
	/** Field 1 of the tuple. */
	public T1 f1;

	/**
	 * Creates a new tuple where all fields are null.
	 */
	public HomoTuple2() {}

	/**
	 * Creates a new tuple and assigns the given values to the tuple's fields.
	 *
	 * @param value0 The value for field 0
	 * @param value1 The value for field 1
	 */
	public HomoTuple2(T0 value0, T1 value1) {
		this.f0 = value0;
		this.f1 = value1;
	}

	@Override
	public int getArity() {
		return 2;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getField(int pos) {
		switch(pos) {
			case 0: return (T) this.f0;
			case 1: return (T) this.f1;
			default: throw new IndexOutOfBoundsException(String.valueOf(pos));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void setField(T value, int pos) {
		switch(pos) {
			case 0:
				this.f0 = (T0) value;
				break;
			case 1:
				this.f1 = (T1) value;
				break;
			default: throw new IndexOutOfBoundsException(String.valueOf(pos));
		}
	}

	/**
	 * Sets new values to all fields of the tuple.
	 *
	 * @param value0 The value for field 0
	 * @param value1 The value for field 1
	 */
	public void setFields(T0 value0, T1 value1) {
		this.f0 = value0;
		this.f1 = value1;
	}

	/**
	* Returns a shallow copy of the tuple with swapped values.
	*
	* @return shallow copy of the tuple with swapped values
	*/
	public HomoTuple2<T1, T0> swap() {
		return new HomoTuple2<T1, T0>(f1, f0);
	}


	/**
	* Shallow tuple copy.
	* @return A new Tuple with the same fields as this.
	*/
	@Override
	@SuppressWarnings("unchecked")
	public HomoTuple2<T0, T1> copy() {
		return new HomoTuple2<>(this.f0,
			this.f1);
	}

	/**
	 * Creates a new tuple and assigns the given values to the tuple's fields.
	 * This is more convenient than using the constructor, because the compiler can
	 * infer the generic type arguments implicitly. For example:
	 * {@code Tuple3.of(n, x, s)}
	 * instead of
	 * {@code new Tuple3<Integer, Double, String>(n, x, s)}
	 */
	public static <T0, T1> HomoTuple2<T0, T1> of(T0 value0, T1 value1) {
		return new HomoTuple2<>(value0,
			value1);
	}
}
