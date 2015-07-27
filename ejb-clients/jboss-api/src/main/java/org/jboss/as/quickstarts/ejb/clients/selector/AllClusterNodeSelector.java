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
package org.jboss.as.quickstarts.ejb.clients.selector;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.ejb.client.ClusterNodeSelector;

/**
 * A simple implementation of a ClusterNodeSelector.
 * This is to demonstrate the behavior and to show the invocation parameters.
 * The behavior (random) is the same as the default selector, but the connected nodes are not respected,
 * the LB will use all available nodes.<br/>
 * If a not connected node is returned it will be opened automaticaly.
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
public class AllClusterNodeSelector implements ClusterNodeSelector {
  private static final Logger LOGGER = Logger.getLogger(AllClusterNodeSelector.class.getName());
  
  @Override
  public String selectNode(final String clusterName, final String[] connectedNodes, final String[] availableNodes) {
    if(LOGGER.isLoggable(Level.FINER)) {
      LOGGER.finer("INSTANCE "+this+ " : cluster:"+clusterName+"  connected:"+Arrays.deepToString(connectedNodes)+" available:"+Arrays.deepToString(availableNodes));
    }
    
    if (availableNodes.length == 1) {
        return availableNodes[0];
    }
    final Random random = new Random();
    final int randomSelection = random.nextInt(availableNodes.length);
    return availableNodes[randomSelection];
  }

}
