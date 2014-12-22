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

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains common SQL Utils.
 *
 * User:
 * Date: 03/12/13
 */
public class SqlUtils {

    // Not using '\' since it has special meaning in MySql
    public static final String ESCAPE_CHAR = "~";
    public static final String ESCAPE_EXPRESSION = "escape '" + ESCAPE_CHAR + "'";

    private static final String DOUBLE_ESCAPE_CHAR = ESCAPE_CHAR + ESCAPE_CHAR;

    private final static String[] NON_MSSQL_SPECIAL_CHARS = {"%", "_"};
	private final static String[] MSSQL_SPECIAL_CHARS;

	static {
		List<String> mssqlSpecialCharsList = new ArrayList<>(Arrays.asList(NON_MSSQL_SPECIAL_CHARS));
		mssqlSpecialCharsList.addAll(Arrays.asList("[","]"));
		MSSQL_SPECIAL_CHARS = mssqlSpecialCharsList.toArray(new String[mssqlSpecialCharsList.size()]);
	}

	@Autowired
	private DataBaseDetector dataBaseDetector;

	// This value is set only once when the Central is up and we don't change our database type
	// while at least one of the servers is still up, so there's no worry that this value might be worng.
	private String[] currentSpecialCharsSet;

	@PostConstruct
	private void setSpecialCharsSet() {
		currentSpecialCharsSet = dataBaseDetector.isMssql()? MSSQL_SPECIAL_CHARS : NON_MSSQL_SPECIAL_CHARS;
	}

	/**
	 * Escaping like expressions so wildcards will not be evaluated.
	 * The HQL query should have the ESCAPE_EXPRESSION in it's suffix for this to work!!!
	 *
	 * The method returns an escaped expression, which would behave the same of all SQL repositories when
	 * adding escape with the ESCAPE_CHAR to the query.
	 *
	 * Note: 'like' expression are evaluated differently in MS-SQL.
	 * '[]' behaves similar to regex expression and we do handle it specifically in MS-SQL.
	 * @param likeExpression The like expression to be escaped.
	 * @return The escaped expression.
	 */
	public String escapeLikeExpression(String likeExpression) {
		String normalizeLikeExpression = likeExpression;
		if (likeExpression != null && !likeExpression.isEmpty()) {
			normalizeLikeExpression = normalizeLikeExpression.replace(ESCAPE_CHAR, DOUBLE_ESCAPE_CHAR);
			for (String charToEscape : currentSpecialCharsSet) {
				normalizeLikeExpression = normalizeLikeExpression.replace(charToEscape, ESCAPE_CHAR + charToEscape);
			}
		}
		return normalizeLikeExpression;
	}

	public String normalizeStartingWithLikeExpression(String expression) {
		return expression + "%";
	}

	public String normalizeEndingWithLikeExpression(String expression) {
		return "%" + expression;
	}

	public String normalizeContainingLikeExpression(String expression) {
		return "%" + expression + "%";
	}

}
