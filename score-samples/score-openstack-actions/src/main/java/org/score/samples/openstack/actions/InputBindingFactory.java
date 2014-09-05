package org.score.samples.openstack.actions;


/**
 * Date: 9/2/2014
 *
 * @author lesant
 */
public class InputBindingFactory {
	public static InputBinding createInputBindingWithDefaultValue(String description, String inputKey, boolean required, String value) {
		return new InputBinding(description, inputKey, required, value);
	}

	public static InputBinding createInputBinding(String description, String inputKey, boolean required) {
		return new InputBinding(description, inputKey, required);
	}

	public static InputBinding createMergeInputBindingWithValue(String destinationKey, String value)
	{
		InputBinding inputBinding = new InputBinding("", "", false);
		inputBinding.setDestinationKey(destinationKey);
		inputBinding.setValue(value);

		return inputBinding;
	}
	public static InputBinding createMergeInputBindingWithSource(String destinationKey, String sourceKey){
		InputBinding inputBinding = new InputBinding("", "", false);
		inputBinding.setDestinationKey(destinationKey);
		inputBinding.setSourceKey(sourceKey);

		return inputBinding;
	}
}
