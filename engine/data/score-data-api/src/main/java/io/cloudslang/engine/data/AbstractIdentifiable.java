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

package io.cloudslang.engine.data;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Date: 12/23/13
 *
 */
@MappedSuperclass
public abstract class AbstractIdentifiable implements Identifiable {
	private static final long serialVersionUID = 3575134062242610091L;

	@Id
	@GeneratedValue(generator = "cs-hilo")
	@GenericGenerator(name = "cs-hilo",
			strategy = "io.cloudslang.engine.data.SimpleHiloIdentifierGenerator")
	@Column(unique = true, nullable = false, name = "ID")
	protected Long id;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();
}