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
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.quickstarts.ejb.clients.selector.CustomScopeEJBClientContextSelector;
import org.jboss.as.quickstarts.ejb.multi.server.app.AppOne;
import org.jboss.as.quickstarts.ejb.multi.server.app.AppTwo;
import org.jboss.ejb.client.EJBClientContext;

/**
 * An advanced example how to use a selector to divide EJB invocations based on a customer business logic.
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
public final class MultithreadCustomScopeClient {
  private static final Logger LOGGER = Logger.getLogger(MultithreadCustomScopeClient.class.getName());
  private static final InitialContext context;
  /**
   * The global Selector to handle all invocations.
   */
  private static final CustomScopeEJBClientContextSelector selector = new CustomScopeEJBClientContextSelector();

  static {
    // suppress logging to the console
    Logger.getLogger("org.jboss.ejb.client").setLevel(Level.SEVERE);
    Logger.getLogger("org.jboss.remoting").setLevel(Level.WARNING);
    Logger.getLogger("org.xnio").setLevel(Level.WARNING);

    Hashtable<String, String> p = new Hashtable<String, String>();
    // tell the JNDI context that we are using the ejb-client library.
    // you might also use the jndi.properties file to set this property
    p.put(InitialContext.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
    try {
      context = new InitialContext(p);
    } catch (NamingException e) {
      throw new RuntimeException("Could not create InitialContext!", e);
    }
    EJBClientContext.setSelector(selector);
    // the clusterName 'ejb' is the default name of the configuration in infinispan and ejb subsystems
    registerClusterScope("appOneClusterAsUser1", "localhost", 4547, "quickuser1", "quick123+", "ejb");

    // register a single node to invoke AppTwo on the app-twoA node
    registerScope("appTwoAsUser", "localhost", 4647, "quickuser", "quick-123");

    // register a single node to invoke AppTwo on the app-twoB node as quickuser2
    registerScope("appTwoAsUser2", "localhost", 5247, "quickuser2", "quick+123");

    // create a scope where two unclustered nodes are used to handle the requests to AppTwo
    Properties props = connectionProperties(null, "A", "localhost", 4647, "quickuser1", "quick123+");
    props = connectionProperties(props, "B", "localhost", 5247, "quickuser2", "quick+123");
    selector.registerEJBClientContext("appTwoMixedUsers", props);
  }

  /**
   * Create a context with a specific parameter and register it it is also possible to add more than one connection with
   * different parameters to the scope.
   * 
   * @param scopeName the name to reference the scope from client code
   * @param host the initial host name
   * @param port the initial port
   * @param userName the name of the connection user, if <code>null</code> no credentials will be added
   * @param password if a userName was given a password for it
   */
  private static void registerScope(String scopeName, String host, int port, String userName, String password) {
    Properties p = connectionProperties(null, "default", host, port, userName, password);
    LOGGER.info("Register Scope '" + scopeName + "' with properties=" + p);
    selector.registerEJBClientContext(scopeName, p);
  }

  private static void registerClusterScope(String scopeName, String initialHost, int initialPort, String userName,
      String password, String clusterName) {
    Properties p = connectionProperties(null, "initialNode", initialHost, initialPort, userName, password);

    if (p.containsKey("remote.clusters")) {
      String con = p.getProperty("remote.clusters").trim();
      p.setProperty("remote.clusters", con + "," + clusterName);
    } else {
      p.put("remote.clusters", clusterName);
    }

    p.put("remote.cluster." + clusterName + ".connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
    p.put("remote.cluster." + clusterName + ".connect.options.org.xnio.Options.SSL_ENABLED", "false");
    if (userName != null) {
      p.put("remote.cluster." + clusterName + ".username", userName);
      p.put("remote.cluster." + clusterName + ".password", password);
    }
    LOGGER.info("Register ClusterScope '" + scopeName + "' with properties=" + p);
    selector.registerEJBClientContext(scopeName, p);
  }

  /**
   * Add the connection properties to the given list.
   * <p>
   * If new properties are created the global settings are added.
   * </p>
   * 
   * @param properties The ejb-client properties, if <code>null</code> a new list will be created
   * @param connectionName the name of the connection to add to the list
   * @param host the initial host name
   * @param port the initial port
   * @param userName the name of the connection user, if <code>null</code> no credentials will be added
   * @param password if a userName was given a password for it
   * @return a reference to the given <code>properties</code> or a new created one
   */
  private static Properties connectionProperties(Properties properties, String connectionName, String host, int port,
      String userName, String password) {
    connectionName = connectionName.trim();
    if (properties == null) {
      properties = new Properties();
      properties.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
    }
    if (properties.containsKey("remote.connections")) {
      String con = properties.getProperty("remote.connections").trim();
      properties.setProperty("remote.connections", con + "," + connectionName);
    } else {
      properties.put("remote.connections", connectionName);
    }

    // add the necessary connection properties
    // properties.put("remote.connection."+connectionName+".connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
    // properties.put("remote.connection."+connectionName+".connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
    // properties.put("remote.connection."+connectionName+".connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS",
    // "JBOSS-LOCAL-USER");
    properties.put("remote.connection." + connectionName + ".port", String.valueOf(port));
    properties.put("remote.connection." + connectionName + ".host", host);

    if (userName != null) {
      properties.put("remote.connection." + connectionName + ".username", userName);
      properties.put("remote.connection." + connectionName + ".password", password);
    }
    return properties;
  }

  /**
   * @param args
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException {
    final Thread t1 = new Thread(new AppOneCaller("appOneClusterAsUser1", 20, 500), "T1");
    final Thread t2 = new Thread(new AppTwoCaller("appTwoAsUser", 20, 200), "T2");
    final Thread t3 = new Thread(new AppTwoCaller("appTwoAsUser2", 10, 700), "T3");
    final Thread t4 = new Thread(new AppTwoCaller("appTwoMixedUsers", 20, 200), "T4");

    t1.start();
    t2.start();
    t3.start();
    t4.start();

    while(t1.isAlive() || t2.isAlive() || t3.isAlive() || t4.isAlive()) {
      Thread.sleep(200);
    }
  }

  /**
   * A runner to invoke the AppOne EJB in a thread context.
   */
  private static class AppOneCaller extends AppCaller {
    AppOne bean = null;

    AppOneCaller(String scope, int noOfInvocations, long snoozeInMillies) {
      super(scope, noOfInvocations, snoozeInMillies);
    }

    @Override
    protected String getStringResult() {
      return bean.getJBossNodeName();
    }

    @Override
    protected void lookup() throws NamingException {
      this.bean = (AppOne) context.lookup("ejb:appone/ejb/AppOneBean!" + AppOne.class.getName());
    }
  }

  /**
   * A runner to invoke the AppTwo EJB in a thread context.
   */
  private static class AppTwoCaller extends AppCaller {
    AppTwo bean = null;

    AppTwoCaller(String scope, int noOfInvocations, long snoozeInMillies) {
      super(scope, noOfInvocations, snoozeInMillies);
    }

    @Override
    protected String getStringResult() {
      return bean.invoke(null);
    }

    @Override
    protected void lookup() throws NamingException {
      this.bean = (AppTwo) context.lookup("ejb:apptwo/ejb/AppTwoBean!" + AppTwo.class.getName());
    }
  }

  /**
   * Base class to invoke the example EJB's of the ejb-multi-server quickstart.
   */
  private static abstract class AppCaller implements Runnable {
    private final String scope;
    private final Logger log;
    private final int invocations;
    private final long snoozetime;

    AppCaller(String scope, int noOfInvocations, long snoozeInMillies) {
      this.scope = scope;
      this.invocations = noOfInvocations;
      this.log = Logger.getLogger(scope);
      this.snoozetime = snoozeInMillies;
    }

    /**
     * Lookup the proxy for later invocation
     * 
     * @throws NamingException
     */
    protected abstract void lookup() throws NamingException;

    /**
     * Invoke a method on the EJB which return a simple identifier.
     * 
     * @return
     */
    protected abstract String getStringResult();

    @Override
    public void run() {
      // set the scope for the running thread
      selector.setScope(this.scope);
      try {
        lookup();
        HashMap<String, Integer> results = new HashMap<String, Integer>();
        for (int i = 1; i <= this.invocations; i++) {
          String nodeName = getStringResult();
          if (results.containsKey(nodeName)) {
            results.put(nodeName, results.get(nodeName) + 1);
          } else {
            results.put(nodeName, 1);
          }
          log.finer("Invocation at server : " + nodeName);
          if (this.snoozetime > 0) {
            try {
              Thread.sleep(this.snoozetime);
            } catch (InterruptedException e) {
              // ignored
            }
          }
        }
        log.info("Finished with scope : " + scope + "  Calls => " + results);
      } catch (NamingException e) {
        log.log(Level.SEVERE, "EJB not found", e);
      }
    }
  }
}
