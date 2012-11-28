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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.quickstarts.ejb.multi.server.app.AppOne;

/**
 * A simple standalone client to demonstrate how to call clustered EJB's with the  jboss-ejb-client.properties file
 * configuration.
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
public class ClusterClient {
  private static final Logger LOGGER = Logger.getLogger(ClusterClient.class.getName());
  private final InitialContext context;

  /**
   * @param debug <code>null</code> suppress other messages except this client info messages, <code>FALSE</code> will set all to
   *        INFO, <code>true</code> set verbose level
   * @throws NamingException
   */
  private ClusterClient(Boolean debug) throws NamingException {
    Level l = debug == null ? Level.SEVERE : debug.booleanValue() ? Level.ALL : Level.INFO;
    Logger.getLogger("").setLevel(l);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
    Logger.getLogger(ClusterClient.class.getPackage().getName()).setLevel(debug != null ? Level.FINEST : Level.INFO);

    Properties props = new Properties();
    props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
    context = new InitialContext(props);
  }

  private void callAppOne(int noOfInvocations) throws NamingException {
    final HashMap<String, Integer> invocations = new HashMap<String, Integer>();
    AppOne appOne = (AppOne)context.lookup("ejb:appone/ejb//AppOneBean!" + AppOne.class.getName());
    for (int i = 0; i < noOfInvocations; i++) {
      final String result = appOne.getJBossNodeName();
      LOGGER.info("Invocation AppOne #"+i+" at node "+result);
      if(invocations.containsKey(result)) {
        invocations.put(result, invocations.get(result)+1);
      }else{
        invocations.put(result, 1);
      }
    }
    LOGGER.info("#"+noOfInvocations+" used servers:  "+invocations);
  }
  
  /**
   * @param args it is possible to change the logging behavior. If no debug parameter is given
   *        only this class is verbose in INFO mode.
   *        <ul>
   *        <li>-d flag, whether all classes should be in INFO mode.</li>
   *        <li>-D flag, whether all classes should be in verbose TRACE mode</li>
   *        </ul>
   * 
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    Boolean debug = null;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-d")) {
        debug = debug == null ? Boolean.FALSE : Boolean.TRUE;
      } else if (args[i].equals("-D")) {
        debug = Boolean.TRUE;
      }
    }
    
    ClusterClient main = new ClusterClient(debug);
    main.callAppOne(50);
    }

}
