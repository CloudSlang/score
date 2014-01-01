package com.hp.oo.execution.debug;

import java.util.Map;

/**
 * User: hajyhia
 * Date: 2/24/13
 * Time: 3:36 PM
 */
public class Breakpoint extends AbstractExecutionInterrupt {
    private static final long serialVersionUID = -5894399998224560579L;

    public static final Breakpoint UNIVERSAL_BREAKPOINT = new Breakpoint("8891cfff-be02-4c61-894c-f33b32ec7bff", null) {
        private static final long serialVersionUID = 2451920242394345806L;
    };

    public Breakpoint() {
        super();
    }
    public Breakpoint(Map<String, String> interruptData) {
        super(interruptData);
    }

    public Breakpoint(String uuid, Map<String, String> interruptData) {
        super(uuid, interruptData);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
