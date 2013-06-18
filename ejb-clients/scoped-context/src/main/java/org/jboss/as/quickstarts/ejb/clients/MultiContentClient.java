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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.quickstarts.ejb.multi.server.app.AppOne;
import org.jboss.as.quickstarts.ejb.multi.server.app.AppTwo;
import org.jboss.as.quickstarts.ejb.multi.server.app.MainApp;

public class MultiContentClient {
    private static final Logger LOGGER = Logger.getLogger(MultiContentClient.class.getName());

    private static final String remoteBeanIdentifierAppMain = "ejb:appmain/ejb//MainAppBean!" + MainApp.class.getName();
    private static final String remoteBeanIdentifierApp1 = "ejb:appone/ejb//AppOneBean!" + AppOne.class.getName();
    private static final String remoteBeanIdentifierApp2 = "ejb:apptwo/ejb//AppTwoBean!" + AppTwo.class.getName();
    
    private static final String SERVER_NAME_APPMAIN = "master:app-main";
    private static final String SERVER_NAME_APPONE = "master:app-one";
    private static final String SERVER_NAME_APPONEA= "master:app-oneA";
    private static final String SERVER_NAME_APPTWO= "master:app-two";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final EJBClientConnectionConfiguration server_AppMain_Conf = new EJBClientConnectionConfiguration();
        server_AppMain_Conf.addServer("localhost", 4447, "quickuser", "quick-123");
        final EJBClientConnectionConfiguration server_AppOne_Conf = new EJBClientConnectionConfiguration();
        server_AppOne_Conf.addServer("localhost", 4547, "quickuser", "quick-123");
        final EJBClientConnectionConfiguration server_AppOneA_Conf = new EJBClientConnectionConfiguration();
        server_AppOneA_Conf.addServer("localhost", 5447, "quickuser2", "quick+123");
        final EJBClientConnectionConfiguration server_AppTwo_Conf = new EJBClientConnectionConfiguration();
        server_AppTwo_Conf.addServer("localhost", 4647, "quickuser1", "quick123+");
        final EJBClientConnectionConfiguration servers_One_Conf = new EJBClientConnectionConfiguration();
        servers_One_Conf.addServer("localhost", 4547, "quickuser", "quick-123");
        servers_One_Conf.addServer("localhost", 5447, "quickuser1", "quick123+");
        
        MainApp serverAppMain_MainApp;
        AppOne serverAppOne_App1;
        AppOne serverAppOneA_App1;
        AppTwo serverAppTwo_App2;
        AppOne servers4AppOne_App1;
        
        try {
            // prepare proxy for main server MainApp
            InitialContext ic = new InitialContext(server_AppMain_Conf.getEJBClientConfiguration());
            serverAppMain_MainApp = (MainApp) ic.lookup(remoteBeanIdentifierAppMain);
            ic.close();
        } catch (NamingException e) {
            throw new RuntimeException("Can not initialize proxies for server main!",e);
        }
        try {
            // prepare proxy for serverOne
            InitialContext ic = new InitialContext(server_AppOne_Conf.getEJBClientConfiguration());
            serverAppOne_App1 = (AppOne) ic.lookup(remoteBeanIdentifierApp1);
            ic.close();
        } catch (NamingException e) {
            throw new RuntimeException("Can not initialize proxies for server one!",e);
        }

        try {
            // prepare proxy for serverOneA
            InitialContext ic = new InitialContext(server_AppOneA_Conf.getEJBClientConfiguration());
            serverAppOneA_App1 = (AppOne) ic.lookup(remoteBeanIdentifierApp1);
            ic.close();
        } catch (NamingException e) {
            throw new RuntimeException("Can not initialize proxies for server oneA!",e);
        }

        try {
            // prepare proxy for serverTwo
            InitialContext ic = new InitialContext(server_AppTwo_Conf.getEJBClientConfiguration());
            serverAppTwo_App2 = (AppTwo) ic.lookup(remoteBeanIdentifierApp2);
            ic.close();
        } catch (NamingException e) {
            throw new RuntimeException("Can not initialize proxies for server two!",e);
        }

        try {
            // prepare proxy for context with all servers contain AppOne
            // the requests will be loadbalanced
            InitialContext ic = new InitialContext(servers_One_Conf.getEJBClientConfiguration());
            servers4AppOne_App1 = (AppOne) ic.lookup(remoteBeanIdentifierApp1);
            ic.close();
        } catch (NamingException e) {
            throw new RuntimeException("Can not initialize proxies for server oneA!",e);
        }

        boolean resultOk = true;
        
        resultOk &= checkServerNodeName(serverAppMain_MainApp.getJBossNodeName(), SERVER_NAME_APPMAIN);
        resultOk &= checkServerNodeName(serverAppOne_App1.getJBossNodeName(), SERVER_NAME_APPONE);
        resultOk &= checkServerNodeName(serverAppOneA_App1.getJBossNodeName(), SERVER_NAME_APPONEA);
        resultOk &= checkServerNodeName(serverAppOne_App1.getJBossNodeName(), SERVER_NAME_APPONE);
        resultOk &= checkServerNodeName(serverAppOne_App1.getJBossNodeName(), SERVER_NAME_APPONE);
        resultOk &= checkServerNodeName(serverAppTwo_App2.getJBossNodeName(), SERVER_NAME_APPTWO);
        resultOk &= checkServerNodeName(serverAppOneA_App1.getJBossNodeName(), SERVER_NAME_APPONEA);
        resultOk &= checkServerNodeName(serverAppOne_App1.getJBossNodeName(), SERVER_NAME_APPONE);
        resultOk &= checkServerNodeName(serverAppOneA_App1.getJBossNodeName(), SERVER_NAME_APPONEA);
        resultOk &= checkServerNodeName(serverAppOneA_App1.getJBossNodeName(), SERVER_NAME_APPONEA);
        resultOk &= checkServerNodeName(serverAppMain_MainApp.getJBossNodeName(), SERVER_NAME_APPMAIN);
        resultOk &= checkServerNodeName(servers4AppOne_App1.getJBossNodeName(), SERVER_NAME_APPONE,SERVER_NAME_APPONEA);
        resultOk &= checkServerNodeName(servers4AppOne_App1.getJBossNodeName(), SERVER_NAME_APPONE,SERVER_NAME_APPONEA);
        resultOk &= checkServerNodeName(servers4AppOne_App1.getJBossNodeName(), SERVER_NAME_APPONE,SERVER_NAME_APPONEA);
        resultOk &= checkServerNodeName(servers4AppOne_App1.getJBossNodeName(), SERVER_NAME_APPONE,SERVER_NAME_APPONEA);
        
        if(!resultOk) {
            throw new Exception("Not all invocations reach the expected server!");
        }
    }
    
    private static boolean checkServerNodeName(String current, String ...expected) {
        List<String> servers = Arrays.asList(expected);
        if(servers.contains(current)) {
            if(servers.size() == 1) {
                LOGGER.info("Call successfully reached the server '"+current+"'");
            } else {
                LOGGER.info("Call successfully reached the server '"+current+"' out of "+servers);
            }
            return true;
        } else {
            LOGGER.severe("Wrong jboss.node.name, expect " + (servers.size()==1 ? "'"+servers.get(0)+"'" : servers) + " but got '"+current+"'. Looks that the wrong server was selected!");
        }
        return false;
    }
}
