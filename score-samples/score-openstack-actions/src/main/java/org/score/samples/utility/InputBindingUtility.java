package org.score.samples.utility;

import org.apache.commons.lang.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 7/31/2014
 *
 * @author Bonczidai Levente
 */
public class InputBindingUtility {
	public static boolean validateParameterArray(Class<?>[] types, Object[] values) {
		List<BindingConflict> conflicts = getBindingConflicts(types, values);
		return conflicts.isEmpty();
	}

	public static List<BindingConflict> getBindingConflicts(Class<?>[] types, Object[] values)
	{
		List<BindingConflict> conflicts = new ArrayList<>();

		int values_length = values == null ? 0 : values.length;
		for (int i = 0; i < values_length; i++) {
			BindingConflict conflict = verifyValue(types[i], values[i], i);
			if (conflict != null) {
				conflicts.add(conflict);
			}
		}

		return conflicts;
	}

	private static BindingConflict verifyValue(Class<?> expectedClass, Object value, int position) {
		BindingConflict bindingConflict = null;
		if (value == null) {
			bindingConflict = new BindingConflict(ConflictType.NULL, position);
		} else {
			if (!expectedClass.isInstance(value)) {
				if (expectedClass.isPrimitive()) {
					Class<?> wrapperClass = ClassUtils.primitiveToWrapper(expectedClass);
					if (!wrapperClass.isInstance(value)) {
						bindingConflict = new BindingConflict(ConflictType.TYPE_MISMATCH, position);
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

		@Override
		public String toString() {
			return "BindingConflict{" +
					"position=" + position +
					", conflictType=" + conflictType +
					'}';
		}
	}
}
