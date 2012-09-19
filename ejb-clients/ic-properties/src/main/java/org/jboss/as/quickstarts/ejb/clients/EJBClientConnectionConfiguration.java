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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Utility class to handle the JNDI properties of a ejb-client context.
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
public final class EJBClientConnectionConfiguration {
    private final List<ServerConnectionParameter> servers = new ArrayList<ServerConnectionParameter>();
    
    public EJBClientConnectionConfiguration() {}
    
    public EJBClientConnectionConfiguration(ServerConnectionParameter ...connectionParameters) {
        servers.addAll(Arrays.asList(connectionParameters));
    }
    
    /**
     * Add the server to the ejb-client configuration.
     * 
     * @param connectionParameter The server parameter are added to the list of servers (by reference)
     */
    public void addServer(ServerConnectionParameter connectionParameter) {
        servers.add(connectionParameter);
    }
    
    /**
     * Add the server to the ejb-client configuration
     * 
     * @param host Name or IP address of the server instance
     * @param remotingPort Connection port for remoting, <code>null</code> default to 4447 
     * @param username Name of the user for security
     * @param password Password for the given user, mandatory if user is set
     */
    public void addServer(String host, Integer remotingPort, String username, String password) {
        servers.add(new ServerConnectionParameter(host, remotingPort, username, password));
    }
    
    /**
     * Add the parameters for the given server configuration to the ejb-client properies.
     * All relevant connection properties (include the list of connections) are handled.
     * 
     * @param ejbClientContextProps The properties to add the server
     * @param connectionName name of the connection for the list
     * @param serverParams the server configuration parameters
     */
    private static void addServer(Properties ejbClientContextProps, String connectionName, ServerConnectionParameter serverParams) {
        // add the given connection name to the list of connections
        if(ejbClientContextProps.containsKey("remote.connections")) {
            final String connections = (String)ejbClientContextProps.get("remote.connections");
            ejbClientContextProps.put("remote.connections", connections + ","+connectionName);
        }else{
            ejbClientContextProps.put("remote.connections", connectionName);
        }
        // add a property which points to the host server of the given connection name
        ejbClientContextProps.put("remote.connection." + connectionName + ".host", serverParams.getHost());
        // add a property which points to the port on which the server is listening for EJB invocations
        ejbClientContextProps.put("remote.connection." + connectionName + ".port", String.valueOf(serverParams.getRemotingPort()));
        // since we are connecting to a dummy server, we use anonymous user
        ejbClientContextProps.put("remote.connection." + connectionName + ".connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "true");
        
        if(serverParams.getUsername() != null) {
            // add the username and password properties which will be used to establish this connection
            ejbClientContextProps.put("remote.connection." + connectionName + ".username", serverParams.getUsername());
            ejbClientContextProps.put("remote.connection." + connectionName + ".password", serverParams.getPassword());
        }
    }
    
    /**
     * Add all known servers to the ejb-client properties.
     * 
     * @param clientProperties The properties to add all servers
     */
    private void addAllServer(Properties clientProperties) {
        int count = 0;
        for (ServerConnectionParameter connectionParam : this.servers) {
            count++;
            addServer(clientProperties, "Server-"+count, connectionParam);
        }
    }
    
    /**
     * @return the number of configured servers
     */
    public int noOfServers() {
        return this.servers.size();
    }

    /**
     * Provide all properties that are necessary to create a scoped InitialContext.
     * 
     * The following global properties are included:
     * <pre>
     * # Enable the scoped EJB client context which will be tied to the JNDI context
     * org.jboss.ejb.client.scoped.context = true
     * # Handle the ejb: namespace during JNDI lookup
     * java.naming.factory.url.pkgs = org.jboss.ejb.client.naming
     * </pre>
     * The connection parameter are depended to the number of servers, the servers are named 'Server-##':
     * <pre>
     * #the list of available server connections
     * remote.connections = Server-1, Server-2 ....
     * 
     * # for each server the following properties up to the given connection parameters
     * 
     * remote.connection.Server-1.host = localhost
     * remote.conneciton.Server-1.port = 4447
     * # credential information if necessary
     * remote.connection.Server-1.username = quickuser
     * remote.connection.Server-1.password = quick-123
     * 
     * remote.connection.Server-1.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS = true
     * </pre>
     * 
     * @return Properties to create the {@link InitialContext} for lookup scoped proxies
     */
    public Properties getEJBClientConfiguration() {
        if(this.servers.isEmpty()) {
            throw new IllegalStateException("The configuration contain no server!");
        }
        final Properties ejbClientContextProps = new Properties();
        // Property to enable scoped EJB client context which will be tied to the JNDI context
        ejbClientContextProps.put("org.jboss.ejb.client.scoped.context", true);
        // Property which will handle the ejb: namespace during JNDI lookup
        ejbClientContextProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        ejbClientContextProps.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");

        addAllServer(ejbClientContextProps);
        
        return ejbClientContextProps;
    }

}
