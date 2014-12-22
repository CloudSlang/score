/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.schema;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 1/20/14
 *
 * @author
 */
@SuppressWarnings("unused")
public class ScoreNamespaceHandler extends NamespaceHandlerSupport {
	private final Logger logger = Logger.getLogger(getClass());

	private Map<String,String> parsers = new HashMap<String,String>(){{
		put("engine", "org.openscore.schema.EngineBeanDefinitionParser");
		put("worker", "org.openscore.schema.WorkerBeanDefinitionParser");
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
