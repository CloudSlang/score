/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples.openstack.actions;

import java.io.Serializable;

/**
 * Date: 8/12/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class InputBinding implements Serializable{
	private String description;
	private boolean required;
	private boolean hasDefaultValue;
	private String value;
	private String sourceKey;
	private String destinationKey;

    InputBinding(String description, String sourceKey, boolean required, String value) {
		this.description = description;
		this.sourceKey = sourceKey;
		this.required = required;
		this.hasDefaultValue = true;
		this.value = value;
	}

	InputBinding(String description, String sourceKey, boolean required) {
		this.description = description;
		this.sourceKey = sourceKey;
		this.required = required;
		this.hasDefaultValue = false;
	}


	public String getDestinationKey() {
		return destinationKey;
	}

	public void setDestinationKey(String destinationKey) {
		this.destinationKey = destinationKey;
	}
	public String getSourceKey() {
		return sourceKey;
	}

	public boolean hasDefaultValue() {
		return hasDefaultValue;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof InputBinding)) {
			return false;
		}

		InputBinding that = (InputBinding) o;

		return hasDefaultValue == that.hasDefaultValue && required == that.required
				&& !(description != null ? !description.equals(that.description) : that.description != null)
				&& !(sourceKey != null ? !sourceKey.equals(that.sourceKey) : that.sourceKey != null)
				&& !((value != null) ? !value.equals(that.value) : (that.value != null));

	}

	@Override
	public int hashCode() {
		int result = description != null ? description.hashCode() : 0;
		result = 31 * result + (required ? 1 : 0);
		result = 31 * result + (hasDefaultValue ? 1 : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (sourceKey != null ? sourceKey.hashCode() : 0);
		return result;
	}

}
