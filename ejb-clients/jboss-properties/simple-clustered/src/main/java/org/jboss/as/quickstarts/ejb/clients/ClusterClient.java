package org.jboss.as.quickstarts.ejb.clients;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.quickstarts.ejb.multi.server.app.AppOne;

public class ClusterClient {
  private static final Logger LOGGER = Logger.getLogger(ClusterClient.class.getName());
  private final InitialContext context;

  private ClusterClient() throws NamingException {
    Properties props = new Properties();
    props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
    context = new InitialContext(props);

  }

  private static void logging(Level level) {
    // set the global visible level at handler
    Logger.getLogger("").getHandlers()[0].setLevel(level.intValue()>Level.INFO.intValue() ? Level.INFO : level);
    // set the level only for relevant classes
    Logger.getLogger("org.jboss").setLevel(level);
    Logger.getLogger("org.xnio").setLevel(level);
    Logger.getLogger(ClusterClient.class.getName()).setLevel(Level.INFO);
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
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    logging(Level.INFO); // suppress logging
    
    ClusterClient main = new ClusterClient();
    main.callAppOne(50);
    }

}
