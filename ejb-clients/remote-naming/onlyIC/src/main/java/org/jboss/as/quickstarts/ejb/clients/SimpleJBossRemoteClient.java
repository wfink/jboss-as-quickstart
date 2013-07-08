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

import org.jboss.as.quickstarts.ejb.multi.server.app.AppOne;

/**
 * A simple EJB client to demonstrate how to use the remoting project to connect a EJB.<br/>
 * Via command line the following interactions are possible:
 * <ul>
 * <li> -u -p  the user and password will be set</li>
 * <li> -L     credentials will be set to quickuser/quick-123 (the default of the multi-server project) if the client is not running on the same machine</li>
 * <li> -s     additional a secured method will be invoked</li>
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 *
 */
public class SimpleJBossRemoteClient {
    private static final Logger LOGGER = Logger.getLogger(SimpleJBossRemoteClient.class.getName());
    final Boolean secured;
    final boolean local;
    final String user;
    final String password;
    private InitialContext context;
    
    static{
      Logger.getLogger("").setLevel(Level.INFO);
      Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
    }
    public SimpleJBossRemoteClient(Boolean secured, boolean local, String user, String password, Boolean debug) throws NamingException {
        this.secured = secured;
        this.local = local;
        this.user = user;
        this.password = password;

        Level l = debug == null ? Level.OFF : debug.booleanValue() ? Level.ALL : Level.INFO;
        Logger.getLogger("").setLevel(l);
        Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
        LOGGER.setLevel(Boolean.TRUE.equals(debug) ? Level.FINEST : Level.INFO);
    }


    private void createContext() throws NamingException {
        Properties p = new Properties();

        p.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        p.put(javax.naming.Context.PROVIDER_URL, "remote://localhost:4547,remote://localhost:5147");
        p.put("jboss.naming.client.ejb.context", true);
        // set the initial connection timeout to 1sec
        p.put("jboss.naming.client.connect.timeout", "1000");

        if(this.user != null) {
            // If the username is given add to properties
            p.put(javax.naming.Context.SECURITY_PRINCIPAL, this.user);
            p.put(javax.naming.Context.SECURITY_CREDENTIALS, this.password);
        }else if(!this.local) {
            p.put(javax.naming.Context.SECURITY_PRINCIPAL, "quickuser");
            p.put(javax.naming.Context.SECURITY_CREDENTIALS, "quick-123");
        }

        LOGGER.info("PARAMS : "+p);
        this.context = new InitialContext(p);
    }
    
    /**
     * To close the underlying physical connection the created InitialContext needs to be closed!
     * All proxies which are looked up by this context are invalid after the context is closed.
     * 
     * @throws NamingException
     */
    private void closeContext() throws NamingException {
      if(this.context != null) {
        this.context.close();
        this.context = null;
      }
    }
    
    
    private void invoke() throws NamingException {
        final String rcal = "appone/ejb/AppOneBean!" + AppOne.class.getName();
        final AppOne remote = (AppOne) context.lookup(rcal);
        
        // invoke the unsecured method if needed
        if(this.secured == null || Boolean.FALSE.equals(this.secured)) {
            final String result = remote.invoke("Client call at "+new Date());
            LOGGER.info("The unsecured EJB call returns : "+result);
        }
        
        // invoke the secured method if wanted
        if(this.secured != null) {
            LOGGER.info("The secured EJB call returns : "+remote.invokeSecured("Client call secured at "+new Date()));
        }
    }
    
    /**
     * Invoke the appOne bean several times by using the remote-naming approach.
     * 
     * @param args it is possible to change the user/password and whether secured methods should be called
     *             <ul>
     *             <li>-u &lt;username&gt;</li>
     *             <li>-p &lt;password&gt;</li>
     *             <li>-s flag, if given the secured method is called in addition to the unsecured</li>
     *             <li>-S flag, if given only the secured method is called</li>
     *             <li>-l suppress the use of default credentials (ejb-multi-server project), will only work if the server is local</li>
     *             </ul>
     *             
     * @throws NamingException problem with InitialContext creation or lookup
     */
    public static void main(String[] args) throws NamingException {
        Boolean secured = null;
        boolean local = false;
        String user=null, passwd=null;
        Boolean debug = null;
        
        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-s")) {
                if(secured != null && secured.booleanValue() != false) {
                    throw new IllegalArgumentException("Only one of -s or -S can be set!");
                }
                secured = Boolean.FALSE;
            }else if(args[i].equals("-S")) {
                if(secured != null && secured.booleanValue() != true) {
                    throw new IllegalArgumentException("Only one of -s or -S can be set!");
                }
                secured = Boolean.TRUE;
            }else if(args[i].equals("-L")) {
                local = true;
            }else if(args[i].equals("-u")) {
                i++;
                user = args[i];
            }else if(args[i].equals("-p")) {
                i++;
                passwd = args[i];
            }else if(args[i].equals("-d")) {
                debug = debug==null ? Boolean.FALSE : Boolean.TRUE;
            }else if(args[i].equals("-D")) {
                debug = Boolean.TRUE;
            }
        }
        SimpleJBossRemoteClient client = new SimpleJBossRemoteClient(secured, local, user, passwd, debug);
        client.createContext();
        client.invoke();
        client.invoke();
        client.invoke();
        client.invoke();
        client.closeContext();
    }
}
