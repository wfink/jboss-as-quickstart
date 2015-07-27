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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.EJBClientInterceptor;
import org.jboss.ejb.client.EJBClientInvocationContext;

/**
 * Example of an interceptor at client side which can be configures with the {@link EJBClientContext#registerInterceptor(int, EJBClientInterceptor)}.
 * The interceptor catch Exceptions of the EJB invocation and do a retry for some cases.  
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
public class RetryInterceptor implements EJBClientInterceptor {
  private static final Logger LOGGER = Logger.getLogger(RetryInterceptor.class.getName());
  
  @Override
  public void handleInvocation(EJBClientInvocationContext context) throws Exception {
    boolean call = true;
    boolean retried = true;
//    LOGGER.info("before invocation");
    // execute call
    if(call) {
      try {
        context.sendRequest();
        call = false;
      }catch(Exception e) {
        LOGGER.log(Level.FINEST, "Exception catched!", e);
        if(!retried) {
          LOGGER.warning("RETRY");
          retried = true;
        }else{
          throw e;
        }
      }
    }
  }

  @Override
  public Object handleInvocationResult(EJBClientInvocationContext context) throws Exception {
//    LOGGER.info("after invocation");
//    LOGGER.info("before invocation");
    // execute call
      try {
        return context.getResult();
      }catch(Exception e) {
        LOGGER.log(Level.WARNING, "Exception catched!", e);
        Thread.sleep(700);
        Object x = context.getResult();
        LOGGER.severe("Retry succeed!!!!!!!!!!!!!!!");
        return x;
      }
  }

}
