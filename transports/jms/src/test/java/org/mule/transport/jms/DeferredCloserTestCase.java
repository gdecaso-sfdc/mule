/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.jms;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Probe;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.MessageProducer;
import javax.jms.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DeferredCloserTestCase
{

    private BlockingQueue queue = new LinkedBlockingQueue();
    private DeferredJmsResourceCloser thread;
    private JmsConnector connector;

    private MessageProducer producer = mock(MessageProducer.class);
    private Session session = mock(Session.class);
    private AtomicInteger closureCounter = new AtomicInteger();

    @Before
    public void setUpThread()
    {
        thread = new DeferredJmsResourceCloser(connector, queue);
    }

    @Before
    public void setUpJmsConnectorAndResourcesMocks()
    {
        this.queue.clear();
        this.connector = mock(JmsConnector.class);
        this.closureCounter.set(0);
        Answer printAndIncrementCounter = new Answer()
        {
            @Override
            public Void answer(InvocationOnMock invocation)
            {
                System.out.printf("Something arrived to close #%d: %s\n", closureCounter.getAndIncrement(), invocation.getArguments()[0].toString());
                return null;
            }
        };

        doAnswer(printAndIncrementCounter).when(connector).closeQuietly(any(MessageProducer.class));
        doAnswer(printAndIncrementCounter).when(connector).closeQuietly(any(Session.class));

        // Set more readable toString methods
        when(producer.toString()).thenReturn("aReProducer");
        when(session.toString()).thenReturn("aReSession");
    }

    @Test(timeout = 10000)
    public void restartThread() throws InterruptedException
    {
        thread.start();

        // Print sth, wait and again
        queue.put(producer);
        silentSleep(1000);
        queue.put(session);
        silentSleep(1000);

        // interrupt thread
        thread.interrupt();
        silentSleep(1000);
        // Add two thing in queue
        queue.put(producer);
        queue.put(session);
        thread = new DeferredJmsResourceCloser(connector, queue);
        thread.start();

        probeGetsEmptied();
    }

    @Test(timeout = 10000)
    public void closingTest() throws InterruptedException
    {
        queue.put(producer);

        thread.start();

        silentSleep(2000);
        queue.put(session);


        silentSleep(2000);
        queue.put(producer);

        probeGetsEmptied();
    }

    private void probeGetsEmptied()
    {
        new PollingProber(10000, 1000).check(new Probe()
        {
            @Override
            public boolean isSatisfied()
            {
                return queue.isEmpty();
            }

            @Override
            public String describeFailure()
            {
                return "Queue should have been empty";
            }
        });
    }

    private void silentSleep(Integer millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            System.out.println("Main thread interrupted");
        }
    }
}
