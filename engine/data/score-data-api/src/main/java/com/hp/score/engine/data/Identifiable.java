package com.hp.score.engine.data;

import java.io.Serializable;

/**
 * Date: 12/23/13
 *
 * @author Dima Rassin
 */
public interface Identifiable extends Serializable {
	/**
	 * Returns the database id.
	 *
	 * @return the database id, or <code>null</code> for a transient entity.
	 */
	Serializable getId();
}
