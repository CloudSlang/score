package com.hp.oo.openstack.actions;

import org.apache.log4j.Logger;

/**
 * Created by bonczida on 7/11/2014.
 */
public class Log4jSetUp {
    private static  final Logger logger = Logger.getLogger(Log4jSetUp.class);

    public static void main(String[] args)
    {
        String[] arguments = {"firstArg", "secondArg", "thirdArgument"};

        logger.trace("Trace Message!");
        logger.debug("Debug Message!");
        logger.info("Info Message!");
        logger.warn("Warn Message!");
        logger.error("Error Message!");
        logger.fatal("Fatal Message!");
    }
}
