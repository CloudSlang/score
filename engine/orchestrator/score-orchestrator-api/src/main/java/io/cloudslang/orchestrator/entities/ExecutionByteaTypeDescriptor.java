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

package io.cloudslang.orchestrator.entities;

import io.cloudslang.score.facade.entities.Execution;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.SerializableTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongVarbinaryTypeDescriptor;

public class ExecutionByteaTypeDescriptor extends AbstractSingleColumnStandardBasicType<Execution> {

    public ExecutionByteaTypeDescriptor() {
        super(LongVarbinaryTypeDescriptor.INSTANCE, new SerializableTypeDescriptor<>(Execution.class));
    }

    public String getName() {
        return Execution.class.getName();
    }
}