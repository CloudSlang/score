package com.ambientideas;

/**
 * Hello again
 * Hello world!
 * Hello
 */

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

public class App 
{
    static Logger logger = Logger.getLogger(App.class);
	// Just a comment - and another one....
    // ANNNNND another comment
    public static void main( String[] args )
    {
    	//Comment
        BasicConfigurator.configure();
        logger.info("Entering application.");
        System.out.println( "Hello World!" );
        //test2
    }
}

//foo