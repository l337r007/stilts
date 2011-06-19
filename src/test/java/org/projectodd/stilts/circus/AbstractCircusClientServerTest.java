/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.projectodd.stilts.circus;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.junit.After;
import org.junit.Before;
import org.projectodd.stilts.MessageAccumulator;
import org.projectodd.stilts.circus.server.AbstractCircusServer;
import org.projectodd.stilts.client.AbstractStompClient;
import org.projectodd.stilts.logging.SimpleLoggerManager;
import org.projectodd.stilts.logging.SimpleLoggerManager.Level;
import org.projectodd.stilts.server.BasicStompServer;

import com.arjuna.ats.jta.common.jtaPropertyManager;

public abstract class AbstractCircusClientServerTest<T extends AbstractCircusServer> {

    public static Level SERVER_ROOT_LEVEL = Level.INFO;
    public static Level CLIENT_ROOT_LEVEL = Level.NONE;

    private TransactionManager transactionManager;
    private T server;
    protected SimpleLoggerManager serverLoggerManager;
    protected SimpleLoggerManager clientLoggerManager;
    protected AbstractStompClient client;

    private final Map<String, MessageAccumulator> accumulators = new HashMap<String, MessageAccumulator>();

    @Before
    public void resetAccumulators() {
        this.accumulators.clear();
    }

    @Before
    public void startServer() throws Throwable {
        setUpServerLoggerManager();
        setUpTransactionManager();
        this.server = createServer();
        this.server.setTransactionManager( this.transactionManager );
        this.server.start();
        prepareServer();
    }
    
    protected void setUpTransactionManager() {
        this.transactionManager = jtaPropertyManager.getJTAEnvironmentBean().getTransactionManager();
    }
    
    public void setUpServerLoggerManager() {
        this.serverLoggerManager = new SimpleLoggerManager( System.err, "server" );
        this.serverLoggerManager.setRootLevel( SERVER_ROOT_LEVEL );
    }
    
    protected abstract T createServer() throws Exception;
    
    public void prepareServer() throws Exception {
        
    }
    
    public T getServer() {
        return this.server;
    }
                          

    @Before
    public void setUpClient() throws Exception {
        setUpClientLogger();
        InetSocketAddress address = new InetSocketAddress( "localhost", BasicStompServer.DEFAULT_PORT );
        this.client = new AbstractStompClient( address );
        this.client.setLoggerManager( this.clientLoggerManager );
    }

    public void setUpClientLogger() {
        this.clientLoggerManager = new SimpleLoggerManager( System.err, "client" );
        this.clientLoggerManager.setRootLevel( CLIENT_ROOT_LEVEL );
    }

    @After
    public void stopServer() throws Throwable {
        this.server.stop();
    }
    
    public MessageAccumulator accumulator(String name, boolean shouldAck, boolean shouldNack) {
        MessageAccumulator accumulator = this.accumulators.get( name );
        if (accumulator == null) {
            accumulator = new MessageAccumulator( shouldAck, shouldNack );
            this.accumulators.put( name, accumulator );
        }

        return accumulator;
    }

    public MessageAccumulator accumulator(String name) {
        return accumulator( name, false, false );
    }

}