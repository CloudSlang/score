package org.score.samples.openstack.actions;

import java.io.Serializable;

/**
 * Date: 8/12/2014
 *
 * @author Bonczidai Levente
 */
public class InputBinding implements Serializable{
	private String inputName;
	private boolean required;

	public InputBinding(String inputName, boolean required) {
		this.inputName = inputName;
		this.required = required;
	}

	public String getInputName() {
		return inputName;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public static class InputBindingException extends Exception {
		public InputBindingException(String message) {
			super(message);
		}
	}
}
