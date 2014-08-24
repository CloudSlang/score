package com.hp.score.schema;

import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
* Date: 2/4/14
*
* @author Dima Rassin
*/
class ConfValue {
	private String name;
	private Class<?> clazz;
	private Object defaultValue;

	public ConfValue NAME(String name) {
		this.name = name;
		return this;
	}

	public ConfValue CLASS(Class<?> clazz) {
		this.clazz = clazz;
		return this;
	}

	public ConfValue DEFAULT(Object defaultValue) {
		this.defaultValue = defaultValue;
		if (defaultValue != null) this.clazz = defaultValue.getClass();
		return this;
	}

	public void register(Element element, ParserContext parserContext){
		String attrValue = element != null? element.getAttribute(name): null;
		new BeanRegistrator(parserContext)
				.NAME(name)
				.CLASS(clazz)
				.addConstructorArgValue(fromString(attrValue))
				.register();
	}

	private Object fromString(String value) {
		if (StringUtils.hasText(value)){
			try {
				return clazz.getConstructor(String.class).newInstance(value);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to parse worker configuration attribute [" + name + "] value: " + value, ex);
			}
		} else {
			return defaultValue;
		}
	}
}
