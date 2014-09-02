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
