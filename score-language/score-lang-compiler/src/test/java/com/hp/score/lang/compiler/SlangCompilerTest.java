package com.hp.score.lang.compiler;
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

/*
 * Created by orius123 on 05/11/14.
 */
import com.hp.score.api.ExecutionPlan;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.net.URL;


public class SlangCompilerTest {

//    @Test
//    public void testCompile() throws Exception {
//
//        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfiguration.class);
//
//        Compiler compiler = context.getBean("compiler", Compiler.class);
//
//        List<File> classPath = Arrays.asList(
//                new File("/media/windows-share/order_vm_flow.yml"),
//                new File("/media/windows-share/operations.yml")
//        );
//
//        compiler.compile(new File("/media/windows-share/create_vm_flow.yml"), classPath);
//
//    }

    @Test
    public void testOpCompile() throws Exception {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        SlangCompiler compiler = context.getBean(SlangCompiler.class);

        URL resource = getClass().getResource("/operation.yaml");
        ExecutionPlan executionPlan = compiler.compile(new File(resource.toURI()), null);
        System.out.println(executionPlan.getName());


    }
}