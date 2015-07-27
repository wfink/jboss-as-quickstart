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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.ejb.client.DeploymentNodeSelector;

/**
 * A simple example that balance the load by a factor 2/3/4 calculated by the name of the node (contain 2/3/4 or A/B/C).
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
public class SimpleLoadFactorNodeSelector implements DeploymentNodeSelector {
  private static final Logger LOGGER = Logger.getLogger(SimpleLoadFactorNodeSelector.class.getName());
  private final Map<String, List<String>[]> nodes = new HashMap<String, List<String>[]>();
  private final Map<String, Integer> cursor = new HashMap<String, Integer>();
  
  private ArrayList<String> calculateNodes(Collection<String> eligibleNodes) {
    ArrayList<String> nodeList = new ArrayList<String>();
    
    for (String string : eligibleNodes) {
      if(string.contains("A") || string.contains("2")) {
        nodeList.add(string);
        nodeList.add(string);
      } else if(string.contains("B") || string.contains("3")) {
        nodeList.add(string);
        nodeList.add(string);
        nodeList.add(string);
      } else if(string.contains("C") || string.contains("4")) {
        nodeList.add(string);
        nodeList.add(string);
        nodeList.add(string);
        nodeList.add(string);
      }
    }
    return nodeList;
  }
  
  @SuppressWarnings("unchecked")
  private void checkNodeNames(String[] eligibleNodes, String key) {
    if(!nodes.containsKey(key) || nodes.get(key)[0].size() != eligibleNodes.length || !nodes.get(key)[0].containsAll(Arrays.asList(eligibleNodes))) {
      // must be synchronized as the client might call it concurrent
      synchronized (nodes) {
        if(!nodes.containsKey(key) || nodes.get(key)[0].size() != eligibleNodes.length || !nodes.get(key)[0].containsAll(Arrays.asList(eligibleNodes))) {
          ArrayList<String> nodeList = new ArrayList<String>();
          nodeList.addAll(Arrays.asList(eligibleNodes));
          
          nodes.put(key,  new List[] { nodeList, calculateNodes(nodeList) });
        }
      }
    }
  }

  /**
   * Select the next entry of the nodes for this EJB.
   * 
   * @param key combined app/modul/distinct
   * @return name of the node to call
   */
  private synchronized String nextNode(String key) {
    Integer c = cursor.get(key);
    List<String> nodeList = nodes.get(key)[1];
    
    if(c == null || c >= nodeList.size()) {
      c = Integer.valueOf(0);
    }
    
    String node = nodeList.get(c);
    cursor.put(key, Integer.valueOf(c + 1));
    
    return node;
  }
  
  @Override
  public String selectNode(String[] eligibleNodes, String appName, String moduleName, String distinctName) {
    if (LOGGER.isLoggable(Level.FINER)) {
      LOGGER.finer("INSTANCE " + this + " : nodes:" + Arrays.deepToString(eligibleNodes) + " appName:" + appName + " moduleName:" + moduleName
          + " distinctName:" + distinctName);
    }

    // if there is only one there is no sense to choice
    if (eligibleNodes.length == 1) {
      return eligibleNodes[0];
    }
    final String key = appName + "|" + moduleName + "|" + distinctName;
    
    checkNodeNames(eligibleNodes, key);
    return nextNode(key);
  }
}
