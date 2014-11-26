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
package org.eclipse.score.samples.utility;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Date: 8/18/2014
 *
 * @author Bonczidai Levente
 */
public class ReadInputUtility {
	public static int readIntegerInput(BufferedReader reader, String inputName) {
		boolean validInput = false;
		int intInput = 0;
		while (!validInput) {
			String rawInput = readInput(reader, inputName);
			try {
				intInput = Integer.parseInt(rawInput);
				validInput = true;
			} catch (NumberFormatException ex) {
				System.out.println("Not an integer!");
			}
		}
		return intInput;
	}
	@SuppressWarnings("unused")
	public static String readStepInput(BufferedReader reader, String stepInputName) {
		return readInput(reader, "<INPUT> " + stepInputName);
	}

	public static String  readInput(BufferedReader reader, String inputName) {
		System.out.print(inputName + ": ");
		return readLine(reader);
	}

	public static String readLine(BufferedReader reader) {
		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException ioe) {
			System.out.println("IO error trying to read command");
			System.exit(1);
		}
		return line;
	}
}
