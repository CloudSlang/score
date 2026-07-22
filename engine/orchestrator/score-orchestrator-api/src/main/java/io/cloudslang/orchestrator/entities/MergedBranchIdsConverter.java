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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps the list of merged branch ids of a {@link SuspendedExecution} to/from a single binary column.
 * <p>
 * The value is serialized as a JSON array of strings (e.g. {@code ["uuid1:0","uuid1:1"]}) and stored
 * UTF-8 encoded as bytes, so the column can be a binary LOB ({@code blob.stream.type}). Using JSON keeps
 * the stored structure easy to extend later (for example to a richer object) without a format change.
 */
@Converter
public class MergedBranchIdsConverter implements AttributeConverter<List<String>, byte[]> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<ArrayList<String>> LIST_TYPE = new TypeReference<>() {
    };

    @Override
    public byte[] convertToDatabaseColumn(List<String> branchIds) {
        if (CollectionUtils.isEmpty(branchIds)) {
            return "[]".getBytes(StandardCharsets.UTF_8);
        }

        try {
            return objectMapper.writeValueAsBytes(branchIds);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize merged branch ids to JSON", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(byte[] dbValue) {
        if (dbValue == null || dbValue.length == 0) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(dbValue, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize merged branch ids from JSON", e);
        }
    }

}

