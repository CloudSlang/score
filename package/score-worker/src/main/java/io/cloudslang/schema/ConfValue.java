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
package io.cloudslang.schema;

import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
* Date: 2/4/14
*
* @author
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
		value = System.getProperty("cloudslang.worker." + name, value);
		if (StringUtils.hasText(value)){
			try {
				return clazz.getConstructor(String.class).newInstance(value);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to parse worker configuration attribute [" + name + "] value: " + value + " "+ ex.getMessage(), ex);
			}
		} else {
			return defaultValue;
		}
	}
}
