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

import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.quickstarts.ejb.clients.selector.SimpleLoadFactorNodeSelector;
import org.jboss.as.quickstarts.ejb.multi.server.app.AppTwo;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

/**
 * A simple EJB client to demonstrate how to use the native ejb-client API to call unclustered EJB's.<br/>
 * A Simple NodeSelector is added to show the behaviour.
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 * 
 */
public class SimpleJBossApiClient {
  private static final Logger LOGGER = Logger.getLogger(SimpleJBossApiClient.class.getName());
  final Boolean secured;
  final boolean useDefaultCredentials;
  final String user;
  final String password;


  public SimpleJBossApiClient(Boolean secured, boolean useDefaultUser, String user, String password, Boolean debug) {
    this.secured = secured;
    this.useDefaultCredentials = useDefaultUser;
    this.user = user;
    this.password = password;

    Level l = debug == null ? Level.SEVERE : debug.booleanValue() ? Level.ALL : Level.INFO;
    Logger.getLogger("").setLevel(l);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
    Logger.getLogger(AppOneClusterJBossApiClient.class.getPackage().getName()).setLevel(debug != null ? Level.FINEST : Level.INFO);
  }

  private void createContext() {
    Properties p = new Properties();
    // The endpoint.name is only use at client side and not necessary to set
    p.put("endpoint.name", "nonClustered-client-endpoint");
    p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");

    p.put("deployment.node.selector", SimpleLoadFactorNodeSelector.class.getName());
    p.put("remote.connections", "appTwoA,appTwoB");
    p.put("remote.connection.appTwoA.port", String.valueOf(4647));
    p.put("remote.connection.appTwoA.host", "localhost");
    p.put("remote.connection.appTwoB.port", String.valueOf(5247));
    p.put("remote.connection.appTwoB.host", "localhost");

    if (this.user != null) {
      // If the username is given add to properties
      p.put("remote.connection.appTwoA.username", this.user);
      p.put("remote.connection.appTwoA.password", this.password);
      p.put("remote.connection.appTwoB.username", this.user);
      p.put("remote.connection.appTwoB.password", this.password);
    } else if (this.useDefaultCredentials) {
      p.put("remote.connection.appTwoA.username", "quickuser");
      p.put("remote.connection.appTwoA.password", "quick-123");
      p.put("remote.connection.appTwoB.username", "quickuser");
      p.put("remote.connection.appTwoB.password", "quick-123");
    }

    LOGGER.info("PARAMS : " + p);

    // create the client configuration based on the given properties
    EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(p);
    // create and set the selector
    ContextSelector<EJBClientContext> selector = new ConfigBasedEJBClientContextSelector(cc);
    EJBClientContext.setSelector(selector);
  }

  /**
   * Invoke AppTwoBean, depend on the settings unsecured and secured methods.
   * 
   * @throws NamingException
   */
  private void invoke() throws NamingException {
    Properties props = new Properties();
    props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
    InitialContext context = new InitialContext(props);

    try {
      final String rcal = "ejb:apptwo/ejb//AppTwoBean!" + AppTwo.class.getName();
      final AppTwo remote = (AppTwo) context.lookup(rcal);
  
      // invoke the unsecured method if needed
      if (this.secured == null || Boolean.FALSE.equals(this.secured)) {
        final String result = remote.invoke("Client call at " + new Date());
        LOGGER.info("The unsecured EJB call returns : " + result);
      }
  
      // invoke the secured method if wanted
      if (this.secured != null) {
        LOGGER.info("The secured EJB call returns : " + remote.invokeSecured("Client call secured at " + new Date()));
      }
    }finally{
      context.close();
    }
  }

  /**
   * @param args it is possible to change the user/password and whether secured methods should be called. The logging behavior
   *        can be changed. if no debug parameter is given only this package is verbose in INFO mode.
   * 
   *        <ul>
   *          <li>-u &lt;username&gt;</li>
   *          <li>-p &lt;password&gt;</li>
   *          <li>-U don't use default credentials of the ejb-multi-server project (if no -u given)</li>
   *          <li>-s flag, if given the secured method is called in addition to the unsecured</li>
   *          <li>-S flag, if given only the secured method is called</li>
   *          <li>-d flag, whether all classes should be in INFO mode.</li>
   *          <li>-D flag, whether all classes should be in verbose TRACE mode</li>
   *        </ul>
   * 
   * @throws NamingException problem with InitialContext creation or lookup
   */
  public static void main(String[] args) throws NamingException {
    Boolean secured = null;
    boolean useDefaultCredentials = true;
    String user = null, passwd = null;
    Boolean debug = null;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-s")) {
        if (secured != null && secured.booleanValue() != false) {
          throw new IllegalArgumentException("Only one of -s or -S can be set!");
        }
        secured = Boolean.FALSE;
      } else if (args[i].equals("-S")) {
        if (secured != null && secured.booleanValue() != true) {
          throw new IllegalArgumentException("Only one of -s or -S can be set!");
        }
        secured = Boolean.TRUE;
      } else if (args[i].equals("-U")) {
        useDefaultCredentials = false;
      } else if (args[i].equals("-u")) {
        i++;
        user = args[i];
      } else if (args[i].equals("-p")) {
        i++;
        passwd = args[i];
      } else if (args[i].equals("-d")) {
        debug = debug == null ? Boolean.FALSE : Boolean.TRUE;
      } else if (args[i].equals("-D")) {
        debug = Boolean.TRUE;
      }
    }
    SimpleJBossApiClient client = new SimpleJBossApiClient(secured, useDefaultCredentials, user, passwd, debug);
    client.createContext();
    client.invoke();
    client.invoke();
    client.invoke();
    client.invoke();
    client.invoke();
    client.invoke();
    client.invoke();
    client.invoke();
    client.invoke();
  }
}
