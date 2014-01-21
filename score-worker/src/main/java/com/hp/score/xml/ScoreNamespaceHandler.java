package com.hp.score.xml;

import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 1/20/14
 *
 * @author Dima Rassin
 */
@SuppressWarnings("unused")
public class ScoreNamespaceHandler extends NamespaceHandlerSupport {
	private Map<String,String> parsers = new HashMap<String,String>(){{
		put("engine", "EngineBeanDefinitionParser");
		put("worker", "WorkerBeanDefinitionParser");
	}};

	@Override
	public void init() {
		for (Map.Entry<String, String> entry : parsers.entrySet()) {
			try {
				//noinspection unchecked
				registerBeanDefinitionParser(entry.getKey(), ((Class<? extends BeanDefinitionParser>)Class.forName(entry.getValue())).newInstance());
			} catch (ClassNotFoundException ex) {
				// do nothing
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Failed to create a parser for element <" + entry.getKey() + ">", ex);
			}
		}
	}
}
