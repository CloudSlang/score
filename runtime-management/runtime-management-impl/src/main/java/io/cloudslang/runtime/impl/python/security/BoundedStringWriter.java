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
package io.cloudslang.runtime.impl.python.security;

import java.io.StringWriter;
import java.util.function.Supplier;

/**
 * Basic implementation of a java.io.Writer than throws RuntimeException
 * when attempting to write more than 'maxChars' characters to the buffer
 */
public class BoundedStringWriter extends StringWriter {

    private static final int defaultMaxChars = Integer.getInteger("jython.standardStreams.maxLength", 1000);
    private static final int nullStringLength = "null".length();

    private final Supplier<RuntimeException> exceptionSupplier;
    private final int maxChars;

    public BoundedStringWriter(Supplier<RuntimeException> exceptionSupplier) {
        super();
        this.maxChars = defaultMaxChars;
        this.exceptionSupplier = exceptionSupplier;
    }

    @Override
    public void write(int c) {
        validateBounds(1);
        super.write(c);
    }

    @Override
    public void write(char[] charArray, int off, int len) {
        if (len > 0) {
            validateBounds(len);
        }
        super.write(charArray, off, len);
    }

    @Override
    public void write(String str) {
        validateBounds((str == null) ? nullStringLength : str.length()); // in case of null the string "null" is added
        super.write(str);
    }

    @Override
    public void write(String str, int off, int len) {
        if (len > 0) {
            validateBounds(len);
        }
        super.write(str, off, len);
    }

    @Override
    public StringWriter append(CharSequence csq) {
        validateBounds((csq == null) ? nullStringLength : csq.length()); // in case of null the string "null" is added
        return super.append(csq);
    }

    @Override
    public StringWriter append(CharSequence csq, int start, int end) {
        validateBounds((csq == null) ? nullStringLength : csq.subSequence(start, end).length());
        return super.append(csq, start, end);
    }

    @Override
    public StringWriter append(char c) {
        validateBounds(1);
        return super.append(c);
    }

    private void validateBounds(int toAddNumberOfChars) {
        if ((getBuffer().length() + toAddNumberOfChars) > maxChars) {
            throw exceptionSupplier.get();
        }
    }

}
