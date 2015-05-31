/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

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
