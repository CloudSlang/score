package com.hp.score.samples.utility;

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
