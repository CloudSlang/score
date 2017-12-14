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

package io.cloudslang.worker.execution.reflection;

import io.cloudslang.worker.execution.services.SessionDataHandler;
import io.cloudslang.score.api.ControlActionMetadata;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @since 20/11/2011
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ReflectionAdapterTest {

	private static final Logger logger = Logger.getLogger(ReflectionAdapterTest.class);
	@Autowired
    ReflectionAdapter adapter;

    @Autowired
    private SessionDataHandler sessionDataHandler;

    @Test
	public void executeControlActionTest() {
		ControlActionMetadata metadata = new ControlActionMetadata("io.cloudslang.worker.execution.reflection.ReflectionAdapterTestHelper", "myMethod_1");
		Map<String, Object> map = new HashMap<>();
		map.put("parameter_1", "TEST");
		map.put("parameter_2", 3);
		try {
			adapter.executeControlAction(metadata, map);
		} catch(Exception ex) {
			logger.error("Failed to run method in reflectionAdapter...", ex);
			Assert.fail();
		}
	}

	@Test
	public void executeControlActionTest_2() {
		ControlActionMetadata metadata = new ControlActionMetadata("io.cloudslang.worker.execution.reflection.ReflectionAdapterTestHelper", "myMethod_2");
		Map<String, Object> map = new HashMap<>();
		map.put("parameter_1", 5);
		map.put("parameter_2", 3);
		Integer result = (Integer)adapter.executeControlAction(metadata, map);
		Assert.assertEquals(8, (int)result);
	}

	@Test
	public void executeControlActionTest_3() {
		ControlActionMetadata metadata = new ControlActionMetadata("io.cloudslang.worker.execution.reflection.ReflectionAdapterTestHelper", "myMethod_3");
		Map<String, Object> actionData = new HashMap<>();
		actionData.put("parameter_1", 5);
		actionData.put("parameter_2", 3);
		@SuppressWarnings("unchecked")
		Map<String, ?> result = (Map<String, ?>)adapter.executeControlAction(metadata, actionData);
		Assert.assertNull(result);
	}

	@Test
	public void executeControlActionTest_4() {
		ControlActionMetadata metadata = new ControlActionMetadata("io.cloudslang.worker.execution.reflection.ReflectionAdapterTestHelperNoSpring", "myMethod_4");
		Map<String, Object> actionData = new HashMap<>();
		actionData.put("parameter_1", 5);
		actionData.put("parameter_2", 3);
		@SuppressWarnings("unchecked")
		Map<String, ?> result = (Map<String, ?>)adapter.executeControlAction(metadata, actionData);
		Assert.assertNull(result);
	}

	@Configuration
	static class Context {

		@Bean
		@SuppressWarnings("static-method")
		ReflectionAdapter reflectionAdapter() {
			return new ReflectionAdapterImpl();
		}

		@Bean
		@SuppressWarnings("static-method")
		ReflectionAdapterTestHelper reflectionAdapterTestHelper() {
			return new ReflectionAdapterTestHelper();
		}

        @Bean
        SessionDataHandler sessionDataHandler(){
            return org.mockito.Mockito.mock(SessionDataHandler.class);
        }
	}

}
