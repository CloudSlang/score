package com.hp.oo.openstack.actions;

import junit.framework.TestCase;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

public class HttpPostMockTest{
    private HttpPostMock mock;

    private String userName = "testUserName";
    private String password = "testPassword";
    private String uri = "https://www.google.co.uk/";
    private HttpPostMock.HttpPostCallback callback = null;
    private boolean logThePost = true;

    @Before
    public void setUp() throws Exception {
        mock = new HttpPostMock();
    }

    @Test
    public void testPost() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Layout layout = new SimpleLayout();
        Appender appender = new WriterAppender(layout, out);
        mock.addAppenderToLogger(appender);

        String logMsg;
        String[] logSplit; //lines
        String[] logSplitPar; //split one line

        try {
            mock.post(userName, password, uri, callback, logThePost);

            logMsg = out.toString();
            System.out.println("logMsg:");
            System.out.println(logMsg);

            logSplit = logMsg.split("\r\n");
            //0 - userName
            logSplitPar = logSplit[0].split("[-=]");
            assertEquals(logSplitPar[2], userName);

            //1 - password
            logSplitPar = logSplit[1].split("[-=]");
            assertEquals(logSplitPar[2], password);

            //2 - uri
            assertEquals(logSplit[2], "INFO - uri=" + uri);

            //3 - callback
            logSplitPar = logSplit[3].split("[-=]");
            String cmp = callback==null?"null":callback.toString();
            assertEquals(logSplitPar[2], cmp);

            //4 - logThePost
            cmp = "true";
            cmp = logThePost==false?"false":cmp;
            logSplitPar = logSplit[4].split("[-=]");
            assertEquals(logSplitPar[2], cmp);
        } finally {
            mock.removeAppenderFromLogger(appender);
        }
    }
}