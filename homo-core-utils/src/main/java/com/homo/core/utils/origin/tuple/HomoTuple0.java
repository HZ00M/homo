/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.homo.core.utils.origin.tuple;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.ObjectStreamException;

/**
 * A tuple with 0 fields.
 *
 * <p>The Tuple0 is a soft singleton, i.e., there is a "singleton" instance, but it does
 * not prevent creation of additional instances.</p>
 *
 * @see HomoTuple
 */

@ToString
@EqualsAndHashCode(callSuper = false)
@Builder
public class HomoTuple0 extends HomoTuple {
	private static final long serialVersionUID = 1L;

	// an immutable reusable Tuple0 instance
	public static final HomoTuple0 INSTANCE = new HomoTuple0();

	// ------------------------------------------------------------------------

	@Override
	public int getArity() {
		return 0;
	}

	@Override
	public <T> T getField(int pos) {
		throw new IndexOutOfBoundsException(String.valueOf(pos));
	}

	@Override
	public <T> void setField(T value, int pos) {
		throw new IndexOutOfBoundsException(String.valueOf(pos));
	}

	/**
	 * Shallow tuple copy.
	 * @return A new Tuple with the same fields as this.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public HomoTuple0 copy(){
		return new HomoTuple0();
	}

	// -------------------------------------------------------------------------------------------------
	// standard utilities
	// -------------------------------------------------------------------------------------------------



	// singleton deserialization
	private Object readResolve() throws ObjectStreamException {
		return INSTANCE;
	}
}
