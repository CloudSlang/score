package com.hp.score.lang.compiler;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.net.URL;


public class CompilerTest {

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

        Compiler compiler = context.getBean(Compiler.class);

        URL resource = getClass().getResource("/operation.yaml");
        compiler.compile(new File(resource.toURI()), null);

    }
}