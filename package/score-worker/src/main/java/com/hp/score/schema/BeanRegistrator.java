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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;

/**
 * Date: 1/28/14
 *
 * @author Dima Rassin
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
