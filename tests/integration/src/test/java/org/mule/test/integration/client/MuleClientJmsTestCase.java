/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.test.integration.client;

import org.mule.MuleManager;
import org.mule.providers.jms.JmsConstants;
import org.mule.extras.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOException;

import javax.jms.TextMessage;
import javax.jms.JMSException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class MuleClientJmsTestCase extends FunctionalTestCase {
    public static final int INTERATIONS = 10;

    public MuleClientJmsTestCase() {
        setDisposeManagerPerSuite(true);
    }

    protected String getConfigResources() {
        return "org/mule/test/integration/client/test-client-jms-mule-config.xml";
    }

//     public void testClientSendDirect() throws Exception
//     {
//     MuleClient client = new MuleClient();
//     MuleManager.getConfiguration().setSynchronous(true);
//
//     UMOMessage message = client.sendDirect("TestReceiverUMO", null, "Test Client Send message", null);
//     assertNotNull(message);
//     assertEquals("Received: Test Client Send message", message.getPayload());
//     }
//
//     public void testClientDispatchDirect() throws Exception
//     {
//     MuleClient client = new MuleClient();
//     MuleManager.getConfiguration().setSynchronous(true);
//
//     client.dispatchDirect("TestReceiverUMO", "Test Client dispatch message",
//     null);
//     }
//
//     public void testClientSend() throws Exception
//     {
//     MuleClient client = new MuleClient();
//     MuleManager.getConfiguration().setSynchronous(true);
//     MuleManager.getConfiguration().setRemoteSync(true);
//
//     UMOMessage message = client.send(getDispatchUrl(), "Test Client Send message", null);
//     assertNotNull(message);
//     assertEquals("Received: Test Client Send message", message.getPayload());
//     }
//
//     public void testClientMultiSend() throws Exception
//     {
//     MuleClient client = new MuleClient();
//     MuleManager.getConfiguration().setSynchronous(true);
//     MuleManager.getConfiguration().setRemoteSync(true);
//
//     for(int i = 0;i < INTERATIONS;i++) {
//     UMOMessage message = client.send(getDispatchUrl(), "Test Client Send message " + i, null);
//     assertNotNull(message);
//     assertEquals("Received: Test Client Send message " + i,
//     message.getPayload());
//     }
//     }
//
//     public void testClientMultiDispatch() throws Exception
//     {
//     MuleClient client = new MuleClient();
//     MuleManager.getConfiguration().setSynchronous(false);
//
//     int i = 0;
//     //to init
//     client.dispatch(getDispatchUrl(), "Test Client Send message " + i, null);
//     long start = System.currentTimeMillis();
//     for(i = 0;i < INTERATIONS;i++) {
//     client.dispatch(getDispatchUrl(), "Test Client Send message " + i, null);
//     }
//     long time = System.currentTimeMillis() - start;
//     System.out.println(i + " took " + time + "ms to process");
//     Thread.sleep(1000);
//     }

    public void testClientDispatchAndReceiveOnReplyTo() throws Exception {
        MuleClient client = new MuleClient();
        MuleManager.getConfiguration().setSynchronous(false);

        Map props = new HashMap();
        props.put(JmsConstants.JMS_REPLY_TO, "replyTo.queue");

        long start = System.currentTimeMillis();
        int i = 0;
        for (i = 0; i < INTERATIONS; i++) {
            System.out.println("Sending message " + i);
            client.dispatch(getDispatchUrl(), "Test Client Dispatch message " + i, props);
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("It took " + time + " ms to send " + i + " messages");

        start = System.currentTimeMillis();
        for (i = 0; i < INTERATIONS; i++) {
            UMOMessage message = client.receive("jms://replyTo.queue", 10000);
            assertNotNull("message should not be null from Reply queue", message);
            System.out.println("Count is " + i);
            System.out.println(((TextMessage) message.getPayload()).getText());
        }
        time = System.currentTimeMillis() - start;
        System.out.println("It took " + time + "ms to receive " + i + " messages");

    }

    public String getDispatchUrl() {
        return "jms://test.queue";
    }

}
