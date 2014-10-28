/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.samples.utility;

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
