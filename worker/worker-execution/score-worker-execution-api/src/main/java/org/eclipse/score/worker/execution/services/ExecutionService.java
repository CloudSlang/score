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
package org.eclipse.score.worker.execution.services;

import org.eclipse.score.facade.entities.Execution;

import java.util.List;

/**
 * Date: 8/1/11
 *
 * @author
 */
//TODO: Add Javadoc
public interface ExecutionService {
	Execution execute(Execution execution) throws InterruptedException;
    List<Execution> executeSplit(Execution execution) throws InterruptedException; //returns null in case this execution is paused or cancelled and the split was not done
    boolean isSplitStep(Execution execution);
}
