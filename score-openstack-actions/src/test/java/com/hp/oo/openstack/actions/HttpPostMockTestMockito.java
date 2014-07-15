package com.hp.oo.openstack.actions;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

public class HttpPostMockTestMockito {
    private Logger loggerMock;
    private HttpPostMock httpPostMock;

    @Before
    public void setUp() throws Exception {
        loggerMock = mock(Logger.class);
        httpPostMock = new HttpPostMock(loggerMock);
    }

    @Test
    public void testPost() throws Exception {
        String userName = "testUserName";
        String password = "testPassword";
        String uri = "https://www.google.co.uk/";
        HttpPostMock.HttpPostCallback callback = null;
        boolean logThePost = true;

        //when(loggerMock.info(eq("user"))).thenReturn("user")
    }
}