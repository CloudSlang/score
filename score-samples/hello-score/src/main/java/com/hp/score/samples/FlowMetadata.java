package com.hp.score.samples;

/**
 * Date: 9/9/2014
 *
 * @author Bonczidai Levente
 */
public class FlowMetadata {
	private String identifier;
	private String description;
	private String className;
	private String triggeringPropertiesMethodName;
	private String inputBindingsMethodName;

	public FlowMetadata(String identifier, String description, String className, String triggeringPropertiesMethodName, String inputBindingsMethodName) {
		this.identifier = identifier;
		this.description = description;
		this.className = className;
		this.triggeringPropertiesMethodName = triggeringPropertiesMethodName;
		this.inputBindingsMethodName = inputBindingsMethodName;
	}

	public String getDescription() {
		return description;
	}

	@SuppressWarnings("unused")
	public String getIdentifier() {
		return identifier;
	}

	public String getClassName() {
		return className;
	}

	public String getTriggeringPropertiesMethodName() {
		return triggeringPropertiesMethodName;
	}

	public String getInputBindingsMethodName() {
		return inputBindingsMethodName;
	}
}
