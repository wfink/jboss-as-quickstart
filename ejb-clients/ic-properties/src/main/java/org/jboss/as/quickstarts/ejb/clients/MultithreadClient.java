/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.ejb.clients;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.quickstarts.ejb.multi.server.app.AppOne;

public class MultithreadClient {
    private static final Logger LOGGER = Logger.getLogger(MultithreadClient.class.getName());

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final EJBClientConnectionConfiguration oneConf = new EJBClientConnectionConfiguration();
        final EJBClientConnectionConfiguration twoConf = new EJBClientConnectionConfiguration();
        oneConf.addServer("localhost", 4547, "quickuser", "quick-123");
        twoConf.addServer("localhost", 5447, "quickuser", "quick-123");
        final App1Call one = new App1Call(oneConf, 10, 100);
        final App1Call two = new App1Call(twoConf, 10, 100);
        
        final Thread t1 = new Thread(one, "One");
        final Thread t2 = new Thread(two, "Two");
        t1.start();
        t2.start();

        while (t1.isAlive() || t2.isAlive()) {
            Thread.sleep(500);
        }

        one.printStatistic();
        two.printStatistic();

        if (one.checkNumberOfNodes() && two.checkNumberOfNodes()) {
            LOGGER.info("The allowed number of nodes in the threads are called");
        } else {
            LOGGER.warning("Failed! unexpected number of nodes called from threads!");
        }
    }

    static class App1Call extends EJBcall {
        private static final String remoteBeanIdentifier = "ejb:appone/ejb//AppOneBean!" + AppOne.class.getName();
        private final AppOne proxy;

        App1Call(EJBClientConnectionConfiguration conf, int executions, int sleeptime) throws NamingException {
            super(conf, executions, sleeptime);
            final InitialContext ic = getIC();
            this.proxy = (AppOne) ic.lookup(remoteBeanIdentifier);
            ic.close();
            LOGGER.warning("PROXY : "+proxy);
        }

        @Override
        protected String getJBossNodeName() {
            return this.proxy.invoke("Multithread test calls");
        }

    }

    /**
     * Utility class to handle the concurrent EJB calls.
     */
    static abstract class EJBcall implements Runnable {
        private static final Logger LOGGER = Logger.getLogger(EJBcall.class.getName());
        private final EJBClientConnectionConfiguration connectionConfig;
        private final int executions;
        private final int sleeptime;
        private final HashMap<String, Integer> nodeNames = new HashMap<String, Integer>();

        static {
            LOGGER.setLevel(Level.INFO);
        }

        EJBcall(EJBClientConnectionConfiguration conf, int executions, int sleeptime) throws NamingException {
            this.connectionConfig = conf;
            this.executions = executions;
            this.sleeptime = sleeptime;
        }

        protected abstract String getJBossNodeName();

        protected InitialContext getIC() {
            try {
                LOGGER.info("CONF  "+this.connectionConfig.getEJBClientConfiguration());
                return new InitialContext(this.connectionConfig.getEJBClientConfiguration());
            } catch (NamingException e) {
                throw new RuntimeException("Can not create InitialContext", e);
            }
        }

        @Override
        public void run() {
            for (int i = 1; i <= this.executions; i++) {
                String name = getJBossNodeName();
                if (!nodeNames.containsKey(name)) {
                    nodeNames.put(name, 1);
                    LOGGER.finer("New node reached <" + name + ">  for call #" + i);
                } else {
                    nodeNames.put(name, nodeNames.get(name) + 1);
                    LOGGER.finer("Node " + name + " called for #" + i);
                }
                try {
                    Thread.sleep(this.sleeptime);
                } catch (InterruptedException e) {
                }
            }
            // printStatistic();
        }

        public boolean checkNumberOfNodes() {
            return this.nodeNames.size() == this.connectionConfig.noOfServers();
        }

        public void printStatistic() {
            LOGGER.info("Server call statistic : " + this.nodeNames);
        }
    }
}
