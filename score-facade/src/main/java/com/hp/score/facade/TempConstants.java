package com.hp.score.facade;

/**
 * Created by peerme on 03/09/2014.
 */
//TODO - remove!!
public class TempConstants {

    // the actually entry in the queue that the worker should run. like the name of the worker itself that
    // was chosen from the group
    public static final String ACTUALLY_OPERATION_GROUP = "ACTUALLY_OPERATION_GROUP";

    public static final String DEFAULT_GROUP = "RAS_Operator_Path";

    // For external workers optimization in PreOp
    public static final String CONTENT_EXECUTION_STEP = "content_step";

    // For whether the current step needs to go out to the outbuffer after execution, will be reset after use !
    // 1. going to queue means this step will be persisted to the db and therefor recoverable in case of broker failure
    // 2. will use the stay in the queue mechanism, meaning the next step will go directly to the in-buffer
    public static final String MUST_GO_TO_QUEUE = "MUST_GO_TO_QUEUE";

    // For whether the next step needs to go out to the queue after execution and also go-to the in-buffer.
    public static final String USE_STAY_IN_THE_WORKER = "USE_STAY_IN_THE_WORKER";

    // For the studio debugger events
    public static final String DEBUGGER_MODE = "DEBUGGER_MODE";

    public static final String USE_DEFAULT_GROUP = "USE_DEFAULT_GROUP";

}
