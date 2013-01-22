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
package org.jboss.as.quickstarts.ejb.clients.interceptor;

import java.util.logging.Logger;

import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.EJBClientInterceptor;
import org.jboss.ejb.client.EJBClientInvocationContext;

/**
 * Example of an interceptor at client side which can be configures with the {@link EJBClientContext#registerInterceptor(int, EJBClientInterceptor)}.
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
public class ClientInterceptor implements EJBClientInterceptor {
  private static final Logger LOGGER = Logger.getLogger(ClientInterceptor.class.getName()); 
  static ThreadLocal<String> iParam = new ThreadLocal<String>();
  static ThreadLocal<String> result = new ThreadLocal<String>();
  
  public static void setInvocationParameter(String param) {
    iParam.set(param);
  }
  public static String getResult() {
    return result.get();
  }
  
  @Override
  public void handleInvocation(EJBClientInvocationContext context) throws Exception {
    result.set(null);
    LOGGER.info("before invocation");
    if(iParam.get() != null) {
      LOGGER.info("Set Param = "+iParam.get());
      context.getContextData().put("Param",iParam.get());
    }
    // execute call
    context.sendRequest();
  }

  @Override
  public Object handleInvocationResult(EJBClientInvocationContext context) throws Exception {
    LOGGER.info("after invocation");
    LOGGER.info("ContextData "+context.getContextData());
    if(context.getContextData().get("InvocationTime") != null) {
      result.set(context.getContextData().get("InvocationTime").toString());
      LOGGER.info("Get Result ["+result.get()+"] from Server application");
    }
    return context.getResult();
  }

}
