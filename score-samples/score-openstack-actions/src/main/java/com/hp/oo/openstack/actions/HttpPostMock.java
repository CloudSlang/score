package com.hp.oo.openstack.actions;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;

/**
 * Created by bonczida on 7/14/2014.
 */
public class HttpPostMock {
    private final static Logger logger = Logger.getLogger(HttpPostMock.class);

    public HttpPostMock() {
    }

    public interface HttpPostCallback {
        public void doWithPost(HttpPost httpPost) throws Exception;
    }

    public HttpResponse post(String userName, String password, String uri, HttpPostCallback callback, boolean logThePost) throws Exception {
        logger.info("username=" + userName);
        logger.info("password=" + password);
        logger.info("uri=" + uri);
        logger.info("callback=" + callback);
        logger.info("logThePost=" + logThePost);
        return null;
    }

    public static void main(String[] args) {
        try {
            new HttpPostMock().post("testUserName", "testPassword", "http://stackoverflow.com/questions/11513677/" +
                    "import-org-apache-commons-httpclienttestUri", null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
