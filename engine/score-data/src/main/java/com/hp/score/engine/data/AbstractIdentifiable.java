package com.hp.score.engine.data;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Date: 12/23/13
 *
 * @author Dima Rassin
 */
@MappedSuperclass
public abstract class AbstractIdentifiable implements Identifiable {
	private static final long serialVersionUID = 3575134062242610091L;

	@Id
	@GeneratedValue(generator = "oo-hilo")
	@GenericGenerator(name = "oo-hilo", strategy = "com.hp.score.engine.data.SimpleHiloIdentifierGenerator")
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