/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;

/**
 * Date: 1/28/14
 *
 * @author
 */
public class BeanRegistrator{
	private ParserContext parserContext;
	private String beanName;
	private BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

	public BeanRegistrator(ParserContext parserContext) {
		if (parserContext == null) throw new IllegalArgumentException("parserContext is null");
		this.parserContext = parserContext;
	}

	public BeanRegistrator NAME(String beanName) {
		this.beanName = beanName;
		return this;
	}

	public BeanRegistrator CLASS(Class<?> beanClass) {
		builder.getRawBeanDefinition().setBeanClass(beanClass);
		return this;
	}

	public BeanRegistrator addConstructorArgValue(Object value) {
		builder.addConstructorArgValue(value);
		return this;
	}

	@SuppressWarnings("unused")
	public BeanRegistrator addConstructorArgReference(String beanName) {
		builder.addConstructorArgReference(beanName);
		return this;
	}

	public BeanRegistrator addPropertyValue(String name, Object value) {
		builder.addPropertyValue(name, value);
		return this;
	}

	@SuppressWarnings("unused")
	public BeanRegistrator addPropertyValue(String name, String beanName) {
		builder.addPropertyReference(name, beanName);
		return this;
	}

	public BeanRegistrator addDependsOn(String ... beanNames) {
		if (beanNames != null){
			for (String beanName : beanNames){
				if (StringUtils.hasText(beanName)) builder.addDependsOn(beanName.trim());
			}
		}
		return this;
	}

	public void register(){
		BeanDefinition beanDefinition = builder.getBeanDefinition();

		// generate bean name if not defined
		if (beanName == null || beanName.isEmpty()){
			beanName = parserContext.getReaderContext().generateBeanName(beanDefinition);
		}

		// actually register bean
		parserContext.getRegistry().registerBeanDefinition(beanName, beanDefinition);

		// reset state
		reset();
	}

	private void reset(){
		beanName=null;
		builder = BeanDefinitionBuilder.genericBeanDefinition();
	}
}
