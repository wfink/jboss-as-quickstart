/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.as.quickstarts.ejb.clients.selector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

/**
 * <p>A specific implementation of {@link ContextSelector} to create and select an EJBClientContext for different scopes.<br/>
 * The implementation will be thread save and it will be possible to set the scope in a multithread environment.</p>
 * <p>
 * All created context will be stored and the connections might be keept open until the context is unregistered and disposed.
 * </p>
 *  <b>Example implementation:</b><br/>
 *  <pre>
 *  public class Batch {
 *    private static final InitialContext context;
 *    private static final CustomScopeEJBClientContextSelector selector = new CustomScopeEJBClientContextSelector();
 * 
 *    static {
 *      Hashtable<String, String> p = new Hashtable<String, String>();
 *      p.put(InitialContext.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
 *      try {
 *        context = new InitialContext(p);
 *      } catch (NamingException e) {
 *        throw new RuntimeException("Could not create InitialContext!", e);
 *      }
 *      EJBClientContext.setSelector(selector);
 *      registerScope("localhostUserFoo", "localhost", 4447, "foo", "bar");
 *      registerScope("localhostUserFoo2", "localhost", 4447, "foo2", "bar");
 *    }
 * 
 *    // create a context with a specific parameter and register it
 *    // it is also possible to add more than one connection with different parameters to the scope
 *    private static void registerScope(String scopeName, String host, int port, String userName, String password) {
 *      Properties p = new Properties();
 *      p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
 *      p.put("remote.connections", "default");
 *      p.put("remote.connection.default.port", String.valueOf(port));
 *      p.put("remote.connection.default.host", host);
 *
 *      p.put("remote.connection.default.username", userName);
 *      p.put("remote.connection.default.password", password);
 * 
 *      selector.registerEJBClientContext(scopeName, p);
 *    }
 * 
 *    public void executeRemoteMethod() {
 *      MyRemote x = context.lookup("ejb:app/module//MyBean!MyRemote"); // for the lookup call the selector is not needed ( no server action )
 *      selector.setScope("localhostUserFoo");  // registerScopeWithUserFoo() should be called once before using it
 *      x.doSomething();
 *      selector.clearScope();  // for security if not longer used
 *    }
 *  }
 * </pre>
 * 
 * @author Jaikiran Pai
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
public class CustomScopeEJBClientContextSelector implements ContextSelector<EJBClientContext> {

	private final ThreadLocal<String> currentScope = new ThreadLocal<String>();

	private final Map<String, ContextSelector<EJBClientContext>> scopedContextSelectors = Collections.synchronizedMap(new HashMap<String, ContextSelector<EJBClientContext>>());

	/**
	 * Activate the scope.
	 * 
	 * @param scopeName the name of the scope given by {@link #registerEJBClientContext(String, Properties)}
	 */
	public void setScope(final String scopeName) {
		currentScope.set(scopeName);
	}

	/**
	 * Reset the scope.
	 * Every call to an EJB will be fail until {@link #setScope(String)} is called with a valid scopeName.
	 */
	public void clearScope() {
		this.currentScope.set(null);
	}

	/**
	 * Register a client context with the provided properties.
	 * The scope is activated by calling {@link #setScope(String)} with the given scopeName.
	 * An existing client context with this name will be replaced.
	 * 
	 * @param scopeName the reference name to activate
	 * @param ejbClientContextConfigProperties necessary properties to create a ClientContext
	 * @throws IllegalArgumentException If the scopeName is null or already registered
	 */
	public void registerEJBClientContext(String scopeName, final Properties ejbClientContextConfigProperties) {
		if(scopeName == null) {
			throw new IllegalArgumentException("A scopeName must be given!");
		}
		if(this.scopedContextSelectors.containsKey(scopeName)) {
			throw new IllegalArgumentException("The scope '"+scopeName+" is already registered!");
		}
		final EJBClientConfiguration ejbClientConfiguration = new PropertiesBasedEJBClientConfiguration(ejbClientContextConfigProperties);
		final ConfigBasedEJBClientContextSelector contextSelector = new ConfigBasedEJBClientContextSelector(ejbClientConfiguration);
		scopedContextSelectors.put(scopeName, contextSelector);
	}

	/**
	 * 
	 * @param scopeName the name of the scope that should be removed
	 * @throws IllegalArgumentException if the scopeName is not registered
	 */
	public void unregisterEJBClientContext(String scopeName) {
		if(this.scopedContextSelectors.remove(scopeName) == null) {
			throw new IllegalArgumentException("The scope with name '"+scopeName+"' is not registered!");
		}
	}

	@Override
	public EJBClientContext getCurrent() {
		final String scopeName = this.currentScope.get();
		if (scopeName == null) {
			throw new IllegalStateException("No information available about current scope, cannot select a EJB client context. Call setScope(...) first");
		}
		final ContextSelector<EJBClientContext> contextSelector = this.scopedContextSelectors.get(scopeName);
		if (contextSelector == null) {
			throw new IllegalStateException("No EJB client context selector registered for current scope " + scopeName);
		}
		return contextSelector.getCurrent();
	}
}
