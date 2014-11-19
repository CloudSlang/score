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
package org.eclipse.score.engine.partitions.services;

import org.eclipse.score.engine.partitions.entities.PartitionGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * Date: 4/23/12
 *
 * @author
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PartitionTemplateTest {
	private static final String TABLE_NAME = "TEST_TABLE";

	@Autowired
	private PartitionTemplate template;

	@Autowired
	private PartitionService service;
    
    @Autowired
    private PartitionUtils partitionUtils;


	@Autowired
	ApplicationContext context;

	@Before
	public void before(){
		reset(service);
	}

	@Test
	public void tablesIteratorTest(){
		int size = 4;
		PartitionGroup group = new PartitionGroup(TABLE_NAME, size, 0, 0);
		when(service.readPartitionGroup(TABLE_NAME)).thenReturn(group);

		assertThat(template.reversedTables()).as("from first").hasSize(size).containsExactly(
				partitionUtils.tableName(TABLE_NAME, 1),
				partitionUtils.tableName(TABLE_NAME, 4),
				partitionUtils.tableName(TABLE_NAME, 3),
				partitionUtils.tableName(TABLE_NAME, 2)
		);

		group.setActivePartition(2);
		assertThat(template.reversedTables()).as("from middle").hasSize(size).containsExactly(
				partitionUtils.tableName(TABLE_NAME, 2),
				partitionUtils.tableName(TABLE_NAME, 1),
				partitionUtils.tableName(TABLE_NAME, 4),
				partitionUtils.tableName(TABLE_NAME, 3)
		);

		group.setActivePartition(4);
		assertThat(template.reversedTables()).as("from last").hasSize(size).containsExactly(
				partitionUtils.tableName(TABLE_NAME, 4),
				partitionUtils.tableName(TABLE_NAME, 3),
				partitionUtils.tableName(TABLE_NAME, 2),
				partitionUtils.tableName(TABLE_NAME, 1)
		);
	}

	@Configuration
	static class Configurator{
		@Bean public PartitionService createPartitionManager(){
			return mock(PartitionService.class);
		}

		@Bean(name = TABLE_NAME) public PartitionTemplate template(){
			return new PartitionTemplateImpl();
		}

        @Bean public PartitionUtils partitionUtils() {
            return new PartitionUtils();
        }
	}
}
