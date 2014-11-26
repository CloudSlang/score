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
package org.eclipse.score.samples.utility;

import org.apache.commons.lang.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 7/31/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class InputBindingUtility {
	public static boolean validateParameterArray(Class<?>[] types, Object[] values, boolean nullAllowed) {
		List<BindingConflict> conflicts = getBindingConflicts(types, values, nullAllowed);
		return conflicts.isEmpty();
	}

	public static List<BindingConflict> getBindingConflicts(Class<?>[] types, Object[] values, boolean nullAllowed)
	{
		List<BindingConflict> conflicts = new ArrayList<>();

		int values_length = values == null ? 0 : values.length;
		for (int i = 0; i < values_length; i++) {
			BindingConflict conflict = verifyValue(types[i], values[i], i, nullAllowed);
			if (conflict != null) {
				conflicts.add(conflict);
			}
		}

		return conflicts;
	}

	private static BindingConflict verifyValue(Class<?> expectedClass, Object value, int position, boolean nullAllowed) {
		BindingConflict bindingConflict = null;
		if (value == null) {
			if (nullAllowed) {
				if (expectedClass.isPrimitive()) {
					bindingConflict = new BindingConflict(ConflictType.TYPE_MISMATCH, position);
				}
			} else {
				bindingConflict = new BindingConflict(ConflictType.NULL, position);
			}
		} else {
			if (!expectedClass.isInstance(value)) {
				bindingConflict = new BindingConflict(ConflictType.TYPE_MISMATCH, position);
				if (expectedClass.isPrimitive()) {
					Class<?> wrapperClass = ClassUtils.primitiveToWrapper(expectedClass);
					if (wrapperClass.isInstance(value)) {
						bindingConflict = null;
					}
				}
			}
		}
		return bindingConflict;
	}

	public enum ConflictType {
		NULL, TYPE_MISMATCH
	}

	public static class BindingConflict {
		public BindingConflict(ConflictType conflictType, int position) {
			this.position = position;
			this.conflictType = conflictType;
		}

		private int position;
		private ConflictType conflictType;

		public int getPosition() {
			return position;
		}

		public ConflictType getConflictType() {
			return conflictType;
		}

		@Override
		public String toString() {
			return "BindingConflict{" +
					"position=" + position +
					", conflictType=" + conflictType +
					'}';
		}
	}

	public static class ListInputBindingException extends RuntimeException {
		public ListInputBindingException(String message) {
			super(message);
		}
	}
}
