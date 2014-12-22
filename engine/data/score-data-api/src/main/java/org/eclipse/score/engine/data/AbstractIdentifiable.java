/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.engine.data;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Date: 12/23/13
 *
 * @author
 */
@MappedSuperclass
public abstract class AbstractIdentifiable implements Identifiable {
	private static final long serialVersionUID = 3575134062242610091L;

	@Id
	@GeneratedValue(generator = "oo-hilo")
	@GenericGenerator(name = "oo-hilo", strategy = "org.eclipse.score.engine.data.SimpleHiloIdentifierGenerator")
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