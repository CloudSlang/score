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
package com.hp.score.samples.controlactions;

/**
 * User: stoneo
 * Date: 20/07/2014
 * Time: 17:36
 */
public class NavigationActions {

    private static long nextStep = 0;

    public long nextStepNavigation(){
        nextStep++;
        return nextStep;
    }

    public long simpleNavigation(Long nextStepId){
        return nextStepId;
    }
}
