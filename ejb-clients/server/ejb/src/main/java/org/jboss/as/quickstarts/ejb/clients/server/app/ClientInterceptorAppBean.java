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
package org.jboss.as.quickstarts.ejb.clients.server.app;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.jboss.as.quickstarts.ejb.clients.server.interceptor.ClientPropertyInterceptor;
import org.jboss.as.quickstarts.ejb.multi.server.app.MainApp;
import org.jboss.logging.Logger;

/**
 * <p>
 * The main bean called by the standalone client.
 * </p>
 * <p>
 * The sub applications, deployed in different servers are called direct or via indirect naming to hide the lookup name and use
 * a configured name via comp/env environment.
 * </p>
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
@Stateless
public class ClientInterceptorAppBean implements ClientInterceptorApp {
  private static final Logger LOGGER = Logger.getLogger(ClientInterceptorAppBean.class);
  @Resource
  SessionContext context;
  
  //@EJB(lookup = "ejb:appmain/ejb//MainEjbClient34AppBean!org.jboss.as.quickstarts.ejb.multi.server.app.MainApp")
  @EJB(lookup = "ejb:appmain/ejb//MainAppBean!org.jboss.as.quickstarts.ejb.multi.server.app.MainApp")
  MainApp mainApp;
  
  @EJB
  AppClientBean myAppBean;

  @Override
  @Interceptors(ClientPropertyInterceptor.class)
  public String invoke() {
    final String param = (String)context.getContextData().get("Param");
    if(param != null) {
      LOGGER.info("EJB: Client set 'Param' = "+param);
    }
    final String result = ClientInterceptorAppBean.class.getName() + " invoked";
    LOGGER.info(result);
    context.getContextData().put("EJB answer", "The server answer");
    
    myAppBean.invoke();
//    mainApp.invokeAll("Call from ClientInterceptorApp");
    
    LOGGER.info("ClientContextData ="+context.getContextData());
    return result;
  }
}
