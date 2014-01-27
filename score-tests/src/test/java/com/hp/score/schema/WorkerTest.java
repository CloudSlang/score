package com.hp.score.schema;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Date: 1/21/14
 *
 * @author Dima Rassin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WorkerTest {

	@Test
	public void baseEngineTest(){

	}

	@Configuration
	@ImportResource("META-INF/spring/schema/schemaWorkerTestContext.xml")
	static class Context{
	}
}
