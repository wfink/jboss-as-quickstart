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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.quickstarts.ejb.multi.server.app.AppTwo;

/**
 * A EJB client to demonstrate how to use the remoting project and connect to different users.<br/>
 * The same EJB is called with different user names and the result message show the CallerPrincipal of the servers method.
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 * 
 */
public class MultiUserJBossRemoteClient {
  private static final Logger LOGGER = Logger.getLogger(MultiUserJBossRemoteClient.class.getName());

  /**
   * 
   * @param debug <code>null</code> suppress other messages except this client info messages, <code>FALSE</code> will set all to
   *        INFO, <code>true</code> set verbose level
   * @throws NamingException
   */
  public MultiUserJBossRemoteClient(Boolean debug) throws NamingException {
    Level l = debug == null ? Level.OFF : debug.booleanValue() ? Level.ALL : Level.INFO;
    Logger.getLogger("").setLevel(l);
    LOGGER.setLevel(Boolean.TRUE.equals(debug) ? Level.FINEST : Level.INFO);
  }

  private InitialContext createContext(String username, String password) throws NamingException {
    Properties p = new Properties();

    p.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
    p.put(javax.naming.Context.PROVIDER_URL, "remote://localhost:4647");
    p.put("jboss.naming.client.ejb.context", true);
    p.put(javax.naming.Context.SECURITY_PRINCIPAL, username);
    p.put(javax.naming.Context.SECURITY_CREDENTIALS, password);

    LOGGER.info("PARAMS : " + p);
    return new InitialContext(p);
  }

  private void invoke(String username, String password) throws NamingException {
    InitialContext context;
    try {
      context = createContext(username, password);
      final String rcal = "apptwo/ejb/AppTwoBean!" + AppTwo.class.getName();
      final AppTwo remote = (AppTwo) context.lookup(rcal);

      // If the context is closed here, the method invocation will fail because the connection to the server is closed
      // context.close();

      // invoke the unsecured method and see the user within the result
      final String result = remote.invoke("Client call at " + new Date());
      LOGGER.info("The unsecured EJB call returns : " + result);

      // ensure that the context is close!
      // if the statement is missed all invocations are using the connection
      // created by the first IC
      context.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws NamingException {
    MultiUserJBossRemoteClient client = new MultiUserJBossRemoteClient(null);
    client.invoke("quickuser", "quick-123");
    client.invoke("quickuser1", "quick123+");
    client.invoke("quickuser2", "quick+123");
  }
}
