package com.hp.oo.execution.services;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Date: 6/10/13
 *
 * @author Dima Rassin
 */
@Component
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
