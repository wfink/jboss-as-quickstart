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

  /**
   * 
   * @param debug <code>null</code> suppress other messages except this client info messages, <code>FALSE</code> will set all to
   *        INFO, <code>true</code> set verbose level
   * @throws NamingException
   */
  private static void logging(Boolean debug) {
    Level l = debug == null ? Level.OFF : debug.booleanValue() ? Level.ALL : Level.INFO;
    // set the global visible level at handler
    Logger.getLogger("").getHandlers()[0].setLevel(l.intValue()>Level.INFO.intValue() ? Level.INFO : l);
    Logger.getLogger("").setLevel(l);
    LOGGER.setLevel(Boolean.TRUE.equals(debug) ? Level.FINEST : Level.INFO);
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
    logging(null); // suppress logging
    
    ClusterClient main = new ClusterClient();
    main.callAppOne(50);
    }

}
