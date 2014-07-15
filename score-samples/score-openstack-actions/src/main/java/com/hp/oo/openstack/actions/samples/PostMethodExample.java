package com.hp.oo.openstack.actions.samples;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PostMethodExample {

    public static void main(String args[]) {

        HttpClient client = new HttpClient();
        client.getParams().setParameter("http.useragent", "Test Client");

        BufferedReader br = null;

        PostMethod method = new PostMethod("http://search.yahoo.com/search");
        method.addParameter("p", "\"java2s\"");

        try{
            int returnCode = client.executeMethod(method);

            if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
                System.err.println("The Post method is not implemented by this URI");
                // still consume the response body
                method.getResponseBodyAsString();
            } else {
                br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                String readLine;
                while(((readLine = br.readLine()) != null)) {
                    System.err.println(readLine);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            method.releaseConnection();
            if(br != null) try { br.close(); } catch (Exception fe) {}
        }

    }
}