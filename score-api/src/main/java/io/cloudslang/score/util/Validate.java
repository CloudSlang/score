/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.score.util;

/**
 * Custom validation utility class for commons-lang3 migration.
 *
 * This class provides validation methods that throw IllegalArgumentException for null values,
 * maintaining compatibility with commons-lang 2.x behavior.
 *
 * In commons-lang 2.x, Validate.notNull() threw IllegalArgumentException,
 * but commons-lang3 Validate throws NullPointerException instead.
 * This class bridges that gap for existing code that depends on IllegalArgumentException.
 */
public final class Validate {

    /**
     * Private constructor to prevent instantiation.
     */
    private Validate() {
    }

    /**
     * Validates that the given object is not null.
     *
     * @param object the object to validate
     * @param message the error message to use if validation fails
     * @return the object if validation passes
     * @throws IllegalArgumentException if the object is null
     */
    public static <T> T notNull(T object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
        return object;
    }

    /**
     * Validates that the given object is not null.
     *
     * @param object the object to validate
     * @return the object if validation passes
     * @throws IllegalArgumentException if the object is null
     */
    public static <T> T notNull(T object) {
        if (object == null) {
            throw new IllegalArgumentException("The validated object is null");
        }
        return object;
    }

    /**
     * Validates that the given condition is true.
     *
     * @param expression the boolean expression to validate
     * @param message the error message to use if validation fails
     * @throws IllegalArgumentException if the expression is false
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that the given condition is true, optionally appending a value to the message.
     * Mirrors commons-lang 2.x Validate.isTrue(boolean, String, Object) behavior.
     *
     * @param expression the boolean expression to validate
     * @param message the error message prefix to use if validation fails
     * @param value the value to append to the message (optional, can be null)
     * @throws IllegalArgumentException if the expression is false
     */
    public static void isTrue(boolean expression, String message, Object value) {
        if (!expression) {
            throw new IllegalArgumentException(message + value);
        }
    }

    /**
     * Validates that the given condition is true.
     *
     * @param expression the boolean expression to validate
     * @throws IllegalArgumentException if the expression is false
     */
    public static void isTrue(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException("The validated expression is false");
        }
    }

    /**
     * Validates that the given collection is not empty (not null and not zero-size).
     *
     * @param collection the collection to validate
     * @param message the error message to use if validation fails
     * @throws IllegalArgumentException if the collection is null or empty
     */
    public static void notEmpty(java.util.Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that the given collection is not empty (not null and not zero-size).
     *
     * @param collection the collection to validate
     * @throws IllegalArgumentException if the collection is null or empty
     */
    public static void notEmpty(java.util.Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException("The validated collection is empty");
        }
    }

    /**
     * Validates that the given string is not empty (not null and not zero-length).
     *
     * @param string the string to validate
     * @param message the error message to use if validation fails
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static void notEmpty(String string, String message) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that the given string is not empty (not null and not zero-length).
     *
     * @param string the string to validate
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static void notEmpty(String string) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("The validated string is empty");
        }
    }

    /**
     * Validates that the given string is not blank (not null, not empty, and not just whitespace).
     *
     * @param string the string to validate
     * @param message the error message to use if validation fails
     * @throws IllegalArgumentException if the string is null, empty, or only whitespace
     */
    public static void notBlank(String string, String message) {
        if (string == null || string.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that the given string is not blank (not null, not empty, and not just whitespace).
     *
     * @param string the string to validate
     * @throws IllegalArgumentException if the string is null, empty, or only whitespace
     */
    public static void notBlank(String string) {
        if (string == null || string.trim().isEmpty()) {
            throw new IllegalArgumentException("The validated string is blank");
        }
    }

    /**
     * Validates that the given map is not empty (not null and not zero-size).
     *
     * @param map the map to validate
     * @throws IllegalArgumentException if the map is null or empty
     */
    public static void notEmpty(java.util.Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("The validated map is empty");
        }
    }

    /**
     * Validates that no element of the given array is null.
     *
     * @param array the array to validate
     * @param message the error message to use if validation fails
     * @throws IllegalArgumentException if the array is null or any element is null
     */
    public static void noNullElements(Object[] array, String message) {
        if (array == null) {
            throw new IllegalArgumentException(message);
        }
        for (Object element : array) {
            if (element == null) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    /**
     * Validates that no element of the given array is null.
     *
     * @param array the array to validate
     * @throws IllegalArgumentException if the array is null or any element is null
     */
    public static void noNullElements(Object[] array) {
        noNullElements(array, "The validated array contains null element");
    }

    /**
     * Validates that no element of the given collection is null.
     *
     * @param collection the collection to validate
     * @param message the error message to use if validation fails
     * @throws IllegalArgumentException if the collection is null or any element is null
     */
    public static void noNullElements(java.util.Collection<?> collection, String message) {
        if (collection == null) {
            throw new IllegalArgumentException(message);
        }
        for (Object element : collection) {
            if (element == null) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    /**
     * Validates that no element of the given collection is null.
     *
     * @param collection the collection to validate
     * @throws IllegalArgumentException if the collection is null or any element is null
     */
    public static void noNullElements(java.util.Collection<?> collection) {
        noNullElements(collection, "The validated collection contains null element");
    }
}
