/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.worker.management.services;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Date: 6/10/13
 *
 * @author
 */
public class RetryTemplate {
	private final Logger logger = Logger.getLogger(getClass());

	public final static int INFINITELY = -1;

	public interface RetryCallback{
		void tryOnce();
	}

	public void retry(int maxRetries, long sleepBetweenRetries, RetryCallback callback){
		Validate.notNull(callback, "Callback is null");

		boolean infinity = (maxRetries == INFINITELY);
		for (int i=0; infinity || i<maxRetries-1; i++) {
			try {
				callback.tryOnce();
				return;
			} catch (Exception ex) {
				logger.error("Try #" + (i+1) + " failed on: ", ex);
				if (sleepBetweenRetries > 0) try {
						Thread.sleep(sleepBetweenRetries);
				} catch (InterruptedException iex) {/* do nothing*/}
			}
		}
		callback.tryOnce();
	}
}
