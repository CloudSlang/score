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

package io.cloudslang.worker.management.services;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Date: 6/10/13
 *
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
