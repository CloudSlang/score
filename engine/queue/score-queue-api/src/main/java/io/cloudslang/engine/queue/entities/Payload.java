/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudslang.engine.queue.entities;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.io.Serializable;

/**
 * User:
 * Date: 10/09/12
 * Time: 09:39
 */
public class Payload implements Cloneable, Serializable {
	private static final long serialVersionUID = 1198403948027561284L;

	private byte[] data;

	public Payload() {
		data = null;
	}

	public Payload(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Payload)) return false;

		Payload that = (Payload)obj;

		return new EqualsBuilder()
				.append(this.data, that.data)
				.isEquals();
	}

	@SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
	@Override
	public Object clone() {
		try {
			Payload cloned = (Payload) super.clone();
			cloned.data = ArrayUtils.clone(data);
			return cloned;
		} catch (CloneNotSupportedException e) {
			System.out.println(e);
			return null;
		}
	}
}
