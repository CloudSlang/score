package org.score.samples.openstack.actions;

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
	private String inputKey;

	public static InputBinding createInputBindingWithDefaultValue(String description, String inputKey, boolean required, String value) {
		return new InputBinding(description, inputKey, required, value);
	}

	public static InputBinding createInputBinding(String description, String inputKey, boolean required) {
		return new InputBinding(description, inputKey, required);
	}

	private InputBinding(String description, String inputKey, boolean required, String value) {
		this.description = description;
		this.inputKey = inputKey;
		this.required = required;
		this.hasDefaultValue = true;
		this.value = value;
	}

	private InputBinding(String description, String inputKey, boolean required) {
		this.description = description;
		this.inputKey = inputKey;
		this.required = required;
		this.hasDefaultValue = false;
	}

	public String getInputKey() {
		return inputKey;
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

		if (hasDefaultValue != that.hasDefaultValue) {
			return false;
		}
		if (required != that.required) {
			return false;
		}
		if (description != null ? !description.equals(that.description) : that.description != null) {
			return false;
		}
		if (inputKey != null ? !inputKey.equals(that.inputKey) : that.inputKey != null) {
			return false;
		}
		if (value != null ? !value.equals(that.value) : that.value != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = description != null ? description.hashCode() : 0;
		result = 31 * result + (required ? 1 : 0);
		result = 31 * result + (hasDefaultValue ? 1 : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (inputKey != null ? inputKey.hashCode() : 0);
		return result;
	}

	public static class InputBindingException extends Exception {
		public InputBindingException(String message) {
			super(message);
		}
	}
}
