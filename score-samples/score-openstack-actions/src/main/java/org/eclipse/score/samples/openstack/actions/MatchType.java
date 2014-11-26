/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples.openstack.actions;

/**
 * Date: 7/29/2014.
 *
 * @author lesant
 */
@SuppressWarnings("unused")
public enum MatchType {
//    CONTAINS,
//    EXACT,
//    NOT_EXACT,
//    ALL_WORDS,
//    AT_LEAST_ONE,
//	  NONE,
//    ONE,
//    BEGINS_WITH,
//    ENDS_WITH,
//    ALWAYS_MATCH,
    EQUAL,
    NOT_EQUAL,
    COMPARE_GREATER,
    COMPARE_GREATER_OR_EQUAL,
    COMPARE_LESS,
    COMPARE_LESS_OR_EQUAL,
	DEFAULT
}
