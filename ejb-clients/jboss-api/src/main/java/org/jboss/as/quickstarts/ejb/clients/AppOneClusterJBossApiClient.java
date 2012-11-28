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

import org.jboss.as.quickstarts.ejb.clients.selector.AllClusterNodeSelector;
import org.jboss.as.quickstarts.ejb.multi.server.app.AppOne;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

/**
 * A simple EJB client to demonstrate how to invoke EJB's in a cluster using the native ejb-client API.<br/>
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 * 
 */
public class AppOneClusterJBossApiClient {
  private static final Logger LOGGER = Logger.getLogger(AppOneClusterJBossApiClient.class.getName());
  final Boolean secured;
  final boolean useDefaultCredentials;
  final String user;
  final String password;

  private InitialContext context;
  private AppOne appOneProxy;

  static {
  }

  public AppOneClusterJBossApiClient(Boolean secured, boolean useDefaultUser, String user, String password, Boolean debug) {
    this.secured = secured;
    this.useDefaultCredentials = useDefaultUser;
    this.user = user;
    this.password = password;

    Level l = debug == null ? Level.SEVERE : debug.booleanValue() ? Level.ALL : Level.INFO;
    Logger.getLogger("").setLevel(l);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
    Logger.getLogger(AppOneClusterJBossApiClient.class.getPackage().getName()).setLevel(
        debug != null ? Level.FINEST : Level.INFO);
  }

  private void createContext() {
    Properties p = new Properties();
    // The endpoint.name is only use at client side and not necessary to set
    // p.put("endpoint.name", "client-endpoint");
    p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
    p.put("remote.connections", "appOne");
    p.put("remote.connection.appOne.port", String.valueOf(4547));
    p.put("remote.connection.appOne.host", "localhost");
    p.put("remote.connection.appOne.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS", "JBOSS-LOCAL-USER");
    p.put("remote.connection.appOne.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");

    p.put("remote.clusters", "ejb");
    p.put("remote.cluster.ejb.connect.options.org.xnio.Options.SSL_ENABLED", "false");
    p.put("remote.cluster.ejb.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
    p.put("remote.cluster.ejb.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS", "JBOSS-LOCAL-USER");
    p.put("remote.cluster.ejb.clusternode.selector", AllClusterNodeSelector.class.getName());
    // this property will not work until EJBCLIENT-57 is integrated, the number of initial connected nodes is >>
    p.put("remote.cluster.ejb.max-allowed-connected-nodes", String.valueOf(1));

    if (this.user != null) {
      // If the username is given add to properties
      p.put("remote.connection.appOne.username", this.user);
      p.put("remote.connection.appOne.password", this.password);
      p.put("remote.cluster.ejb.username", this.user);
      p.put("remote.cluster.ejb.password", this.password);
    } else if (this.useDefaultCredentials) {
      p.put("remote.connection.appOne.username", "quickuser");
      p.put("remote.connection.appOne.password", "quick-123");
      p.put("remote.cluster.ejb.username", "quickuser");
      p.put("remote.cluster.ejb.password", "quick-123");
    }

    LOGGER.info("PARAMS : " + p);

    // create the client configuration based on the given properties
    EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(p);
    // create and set the selector
    ContextSelector<EJBClientContext> selector = new ConfigBasedEJBClientContextSelector(cc);
    EJBClientContext.setSelector(selector);
  }

  private void initialize() throws NamingException {
    Properties props = new Properties();
    props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
    this.context = new InitialContext(props);

    final String rcal = "ejb:appone/ejb//AppOneBean!" + AppOne.class.getName();
    this.appOneProxy = (AppOne) context.lookup(rcal);
  }

  private void invoke() throws NamingException {
    // invoke the unsecured method if needed
    if (this.secured == null || Boolean.FALSE.equals(this.secured)) {
      final String result = this.appOneProxy.invoke("Client call at " + new Date());
      LOGGER.info("The unsecured EJB call returns : " + result);
    }

    // invoke the secured method if wanted
    if (this.secured != null) {
      LOGGER.info("The secured EJB call returns : " + this.appOneProxy.invokeSecured("Client call secured at " + new Date()));
    }
  }

  /**
   * @param args it is possible to change the user/password and whether secured methods should be called. The logging behavior
   *        can be changed. if no debug parameter is given only this package is verbose in INFO mode.
   * 
   *        <ul>
   *        <li>-u &lt;username&gt;</li>
   *        <li>-p &lt;password&gt;</li>
   *        <li>-U use default credentials of the ejb-multi-server project (if no -u given)</li>
   *        <li>-s flag, if given the secured method is called in addition to the unsecured</li>
   *        <li>-S flag, if given only the secured method is called</li>
   *        <li>-d flag, whether all classes should be in INFO mode.</li>
   *        <li>-D flag, whether all classes should be in verbose TRACE mode</li>
   *        </ul>
   * 
   * @throws NamingException problem with InitialContext creation or lookup
   */
  public static void main(String[] args) throws NamingException {
    Boolean secured = null;
    boolean useDefaultUser = true;
    String user = null, passwd = null;
    Boolean debug = false;

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
        useDefaultUser = false;
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
    AppOneClusterJBossApiClient client = new AppOneClusterJBossApiClient(secured, useDefaultUser, user, passwd, debug);
    client.createContext();
    client.initialize();

    for (int i = 1; i < 21; i++) {
      client.invoke();
    }
  }
}
