
package com.homo.core.utils.origin.tuple;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
@Builder
public class Tuple10<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> extends Tuple {

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
	/** Field 7 of the tuple. */
	public T7 f7;
	/** Field 8 of the tuple. */
	public T8 f8;
	/** Field 9 of the tuple. */
	public T9 f9;

	/**
	 * Creates a new tuple where all fields are null.
	 */
	public Tuple10() {}

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
	 * @param value7 The value for field 7
	 * @param value8 The value for field 8
	 * @param value9 The value for field 9
	 */
	public Tuple10(T0 value0, T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6, T7 value7, T8 value8, T9 value9) {
		this.f0 = value0;
		this.f1 = value1;
		this.f2 = value2;
		this.f3 = value3;
		this.f4 = value4;
		this.f5 = value5;
		this.f6 = value6;
		this.f7 = value7;
		this.f8 = value8;
		this.f9 = value9;
	}

	@Override
	public int getArity() {
		return 10;
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
			case 7: return (T) this.f7;
			case 8: return (T) this.f8;
			case 9: return (T) this.f9;
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
			case 7:
				this.f7 = (T7) value;
				break;
			case 8:
				this.f8 = (T8) value;
				break;
			case 9:
				this.f9 = (T9) value;
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
	 * @param value7 The value for field 7
	 * @param value8 The value for field 8
	 * @param value9 The value for field 9
	 */
	public void setFields(T0 value0, T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6, T7 value7, T8 value8, T9 value9) {
		this.f0 = value0;
		this.f1 = value1;
		this.f2 = value2;
		this.f3 = value3;
		this.f4 = value4;
		this.f5 = value5;
		this.f6 = value6;
		this.f7 = value7;
		this.f8 = value8;
		this.f9 = value9;
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
	public Tuple10<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> copy() {
		return new Tuple10<>(this.f0,
			this.f1,
			this.f2,
			this.f3,
			this.f4,
			this.f5,
			this.f6,
			this.f7,
			this.f8,
			this.f9);
	}

	/**
	 * Creates a new tuple and assigns the given values to the tuple's fields.
	 * This is more convenient than using the constructor, because the compiler can
	 * infer the generic type arguments implicitly. For example:
	 * {@code Tuple3.of(n, x, s)}
	 * instead of
	 * {@code new Tuple3<Integer, Double, String>(n, x, s)}
	 */
	public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> Tuple10<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T0 value0, T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6, T7 value7, T8 value8, T9 value9) {
		return new Tuple10<>(value0,
			value1,
			value2,
			value3,
			value4,
			value5,
			value6,
			value7,
			value8,
			value9);
	}
}
