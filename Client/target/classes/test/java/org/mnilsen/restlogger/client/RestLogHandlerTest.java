/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mnilsen.restlogger.client;

import java.util.logging.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michaeln
 */
public class RestLogHandlerTest {
    private final Logger logger = Logger.getLogger("org.mnilsen.restlogger.tests");
    private static String TEST_LOG_URL = "http://localhost:8080/RestLogger/webapi/log/write";
    private RestLogHandler handler = null;
    
    public RestLogHandlerTest() {
    }
    
    @Before
    public void setUp() {
        logger.setLevel(Level.ALL);
        logger.addHandler(new ConsoleHandler());
        handler = new RestLogHandler(TEST_LOG_URL);
        this.handler.setLevel(Level.ALL);
        this.logger.addHandler(this.handler);
    }
    
    @After
    public void tearDown() {
    }
    
//    @org.junit.Test
//    public void testAddHandler()
//    {
//        handler = new RestLogHandler(TEST_LOG_URL);
//        this.handler.setLevel(Level.ALL);
//        this.logger.addHandler(this.handler);
//    }
    
    @org.junit.Test
    public void testLogWriting()
    {
        for(int i = 0;i < 10;i++)
        {
            this.logger.log(Level.INFO, "Test message {0}", i);
        }
    }    
    
    @org.junit.Test
    public void testMessagesRejected()
    {
        this.handler.setLevel(Level.SEVERE);
        for(int i = 0;i < 10;i++)
        {
            this.logger.log(Level.INFO, "Test INFO message {0}", i);
        }
    }

    /**
     * Test of getLogUrl method, of class RestLogHandler.
     */
    @org.junit.Test
    public void testGetLogUrl() {
        System.out.println("getLogUrl");
        
        String result = this.handler.getLogUrl();
        assertEquals(TEST_LOG_URL, result);
    }
    
}
