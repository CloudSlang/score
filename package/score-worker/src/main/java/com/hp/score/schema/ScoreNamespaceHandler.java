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
package com.hp.score.schema;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import sun.util.logging.resources.logging_it;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 1/20/14
 *
 * @author Dima Rassin
 */
@SuppressWarnings("unused")
public class ScoreNamespaceHandler extends NamespaceHandlerSupport {
	private final Logger logger = Logger.getLogger(getClass());

	private Map<String,String> parsers = new HashMap<String,String>(){{
		put("engine", "com.hp.score.schema.EngineBeanDefinitionParser");
		put("worker", "com.hp.score.schema.WorkerBeanDefinitionParser");
	}};

	@Override
	public void init() {
		if (logger.isInfoEnabled()) logger.info("Registering Score namespace handler");
		for (Map.Entry<String, String> entry : parsers.entrySet()) {
			try {
				//noinspection unchecked
				registerBeanDefinitionParser(entry.getKey(), ((Class<? extends BeanDefinitionParser>)Class.forName(entry.getValue())).newInstance());
				if (logger.isDebugEnabled()) logger.debug("registered element <" + entry.getKey() + "> with parser " + entry.getValue());
			} catch (ClassNotFoundException ex) {
				// do nothing
				if (logger.isDebugEnabled()) logger.debug("cannot register element <" + entry.getKey() + "> since due to parser class wasn't found: " + entry.getValue());
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Failed to register element <" + entry.getKey() + ">", ex);
			}
		}
	}
}
