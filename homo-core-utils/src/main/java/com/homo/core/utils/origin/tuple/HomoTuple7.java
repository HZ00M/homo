
package com.homo.core.utils.origin.tuple;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
@Builder
public class HomoTuple7<T0, T1, T2, T3, T4, T5, T6> extends HomoTuple {

	private static final long serialVersionUID = 1L;

	/** Field 0 of the tuple. */
	public T0 f0;
	/** Field 1 of the tuple. */
	public T1 f1;
	/** Field 2 of the tuple. */
	public T2 f2;
	/** Field 3 of the tuple. */
	public T3 f3;
	/** Field 4 of the tuple. */
	public T4 f4;
	/** Field 5 of the tuple. */
	public T5 f5;
	/** Field 6 of the tuple. */
	public T6 f6;

	/**
	 * Creates a new tuple where all fields are null.
	 */
	public HomoTuple7() {}

	/**
	 * Creates a new tuple and assigns the given values to the tuple's fields.
	 *
	 * @param value0 The value for field 0
	 * @param value1 The value for field 1
	 * @param value2 The value for field 2
	 * @param value3 The value for field 3
	 * @param value4 The value for field 4
	 * @param value5 The value for field 5
	 * @param value6 The value for field 6
	 */
	public HomoTuple7(T0 value0, T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6) {
		this.f0 = value0;
		this.f1 = value1;
		this.f2 = value2;
		this.f3 = value3;
		this.f4 = value4;
		this.f5 = value5;
		this.f6 = value6;
	}

	@Override
	public int getArity() {
		return 7;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getField(int pos) {
		switch(pos) {
			case 0: return (T) this.f0;
			case 1: return (T) this.f1;
			case 2: return (T) this.f2;
			case 3: return (T) this.f3;
			case 4: return (T) this.f4;
			case 5: return (T) this.f5;
			case 6: return (T) this.f6;
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
			case 2:
				this.f2 = (T2) value;
				break;
			case 3:
				this.f3 = (T3) value;
				break;
			case 4:
				this.f4 = (T4) value;
				break;
			case 5:
				this.f5 = (T5) value;
				break;
			case 6:
				this.f6 = (T6) value;
				break;
			default: throw new IndexOutOfBoundsException(String.valueOf(pos));
		}
	}

	/**
	 * Sets new values to all fields of the tuple.
	 *
	 * @param value0 The value for field 0
	 * @param value1 The value for field 1
	 * @param value2 The value for field 2
	 * @param value3 The value for field 3
	 * @param value4 The value for field 4
	 * @param value5 The value for field 5
	 * @param value6 The value for field 6
	 */
	public void setFields(T0 value0, T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6) {
		this.f0 = value0;
		this.f1 = value1;
		this.f2 = value2;
		this.f3 = value3;
		this.f4 = value4;
		this.f5 = value5;
		this.f6 = value6;
	}


	// -------------------------------------------------------------------------------------------------
	// standard utilities
	// -------------------------------------------------------------------------------------------------

	/**
	* Shallow tuple copy.
	* @return A new Tuple with the same fields as this.
	*/
	@Override
	@SuppressWarnings("unchecked")
	public HomoTuple7<T0, T1, T2, T3, T4, T5, T6> copy() {
		return new HomoTuple7<>(this.f0,
			this.f1,
			this.f2,
			this.f3,
			this.f4,
			this.f5,
			this.f6);
	}

	/**
	 * Creates a new tuple and assigns the given values to the tuple's fields.
	 * This is more convenient than using the constructor, because the compiler can
	 * infer the generic type arguments implicitly. For example:
	 * {@code Tuple3.of(n, x, s)}
	 * instead of
	 * {@code new Tuple3<Integer, Double, String>(n, x, s)}
	 */
	public static <T0, T1, T2, T3, T4, T5, T6> HomoTuple7<T0, T1, T2, T3, T4, T5, T6> of(T0 value0, T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6) {
		return new HomoTuple7<>(value0,
			value1,
			value2,
			value3,
			value4,
			value5,
			value6);
	}
}
