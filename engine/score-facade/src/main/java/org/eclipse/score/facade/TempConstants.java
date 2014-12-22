/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.facade;

/**
 * Created by peerme on 03/09/2014.
 */
public class TempConstants {

    // the actually entry in the queue that the worker should run. like the name of the worker itself that
    // was chosen from the group
    public static final String ACTUALLY_OPERATION_GROUP = "ACTUALLY_OPERATION_GROUP";

    public static final String DEFAULT_GROUP = "RAS_Operator_Path";

    // For external workers optimization in PreOp
    public static final String CONTENT_EXECUTION_STEP = "content_step";

    //This flag is set if the current execution step is important and must be persisted
    public static final String IS_RECOVERY_CHECKPOINT = "IS_RECOVERY_CHECKPOINT";

    // This flag is set if the current execution step needs to go through group resolving
    public static final String SHOULD_CHECK_GROUP = "SHOULD_CHECK_GROUP";

    // For the studio debugger events
    public static final String DEBUGGER_MODE = "DEBUGGER_MODE";

    public static final String USE_DEFAULT_GROUP = "USE_DEFAULT_GROUP";

}
