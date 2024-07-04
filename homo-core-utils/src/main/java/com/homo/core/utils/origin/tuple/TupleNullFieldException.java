

package com.homo.core.utils.origin.tuple;

public class TupleNullFieldException extends RuntimeException
{
	/**
	 * UID for serialization interoperability. 
	 */
	private static final long serialVersionUID = -8820467525772321173L;
	
	private final int fieldPos;

	/**
	 * Constructs an {@code TupleNullFieldException} with {@code null}
	 * as its error detail message.
	 */
	public TupleNullFieldException() {
		super();
		this.fieldPos = -1;
	}

	/**
	 * Constructs an {@code TupleNullFieldException} with the specified detail message.
	 *
	 * @param message The detail message.
	 */
	public TupleNullFieldException(String message) {
		super(message);
		this.fieldPos = -1;
	}
	
	/**
	 * Constructs an {@code TupleNullFieldException} with a default message, referring to
	 * given field number as the null field.
	 *
	 * @param fieldIdx The index of the field that was null, but expected to hold a value.
	 */
	public TupleNullFieldException(int fieldIdx) {
		super("Field " + fieldIdx + " is null, but expected to hold a value.");
		this.fieldPos = fieldIdx;
	}

	/**
	 * Constructs an {@code TupleNullFieldException} with a default message, referring to
	 * given field number as the null field and a cause (Throwable)
	 *
	 * @param fieldIdx The index of the field that was null, but expected to hold a value.
	 * @param cause Pass the root cause of the error
	 */
	public TupleNullFieldException(int fieldIdx, Throwable cause) {
		super("Field " + fieldIdx + " is null, but expected to hold a value.", cause);
		this.fieldPos = fieldIdx;
	}
	
	/**
	 * Gets the field number that was attempted to access. If the number is not set, this method returns
	 * {@code -1}.
	 * 
	 * @return The field number that was attempted to access, or {@code -1}, if not set.
	 */
	public int getFieldPos() {
		return this.fieldPos;
	}
}
