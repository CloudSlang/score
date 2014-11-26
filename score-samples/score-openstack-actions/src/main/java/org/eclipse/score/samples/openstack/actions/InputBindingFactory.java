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
