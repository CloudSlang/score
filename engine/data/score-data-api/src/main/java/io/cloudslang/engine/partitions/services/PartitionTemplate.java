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

package io.cloudslang.engine.partitions.services;

import java.util.List;

/**
 * Date: 4/27/12
 *
 *
 * Trmplate class for handling partiotiond tables
 *
 */
public interface PartitionTemplate {

    /**
     *
     * return the currently active table
     *
     * @return a String of the table name
     */
	String activeTable();

    /**
     *
     * return the previous active table
     *
     * @return s String of the previous active table name
     */
	String previousTable();

    /**
     *
     * returns a List of the reserved tables for the group
     *
     * @return a List of the reserved tables for the group
     */
	List<String> reversedTables();

    /**
     * 
     * rolls to the next partition
     *
     */
	void onRolling();
}
