package com.hp.oo.openstack.actions.samples;

import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

/**
 * Created by bonczida on 7/15/2014.
 */
public class PostMethodExample2 {
    public static void main(String[] args) throws Exception {
        String strURL = "http://www.goldnames.com/view/219";
        String strXMLFilename = "c:\\Workspace\\postXml.xml";
        File input = new File(strXMLFilename);
        // Prepare HTTP post
        PostMethod post = new PostMethod(strURL);
        // Request content will be retrieved directly
        // from the input stream
        RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
        post.setRequestEntity(entity);
        // Get HTTP client
        HttpClient httpclient = new HttpClient();
        // Execute request
        try {
            int result = httpclient.executeMethod(post);
            // Display status code
            System.out.println("Response status code: " + result);
            // Display response
            System.out.println("Response body: ");
            System.out.println(post.getResponseBodyAsString());
        } finally {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }
    }
}