ejb-clients: Standalone clients calling remote EJB applications
======================================================
Author: Wolf-Dieter Fink
Level: Intermediate ... Advanced
Technologies: EJB remote standalone application
Summary: Show how to access EJB's from a remote standalone client


What is it?
-----------

This example demonstrates the communication between standalone clients and JBoss AS7 application server
Also the configuration is done by CLI batch-scripts.


The example is composed of multiple maven projects, each with a shared parent. The projects are as follows:

jboss-api      : Different Main classes to demonstrate the usage of the ejb-client API
jboss-properies: different projects with jboss-ejb-client.poperties
remoting       : Different clients which are using the (deprecated) remoting-project
ic-properties  : Different Main classes to demonstrate how the new scoped InitialContext can be used


The root `pom.xml` builds each of the subprojects in the above order.



System requirements
-------------------

All you need to build this project is Java 6.0 (Java SDK 1.6) or better, Maven 3.0 or better.

The application this project produces is designed to be run on JBoss Enterprise Application Platform 6 or JBoss AS 7. 

 
Configure Maven
---------------

If you have not yet done so, you must [Configure Maven](../README.md#mavenconfiguration) before testing the quickstarts.


Configure necessary Components
-------------------------

This quickstart requires the ejb-multi-server project. Please follow the instruction of these project to ensure that the JBoss-domain is well configured and the application is correct deployed. You may not need the point "Access the Application" but if you run it you are sure that the project works so far.

Start and configure JBoss Enterprise Application Platform 6 or JBoss AS 7
-------------------------

With this quickstart scripts are provided to extend the configuration and deployments of the multi-server quickstart.

 * Make sure you have build the multi-server project.
 * Make sure you have started the JBoss Server and deployed the multi-server applications as described in the multi-server project.
 * Add application user  [Add an Application User](../README.md#addapplicationuser)
   For some of the examples with security there are additional users with roles needed.
   To add all necessary users run the following commands, please use this usernames and paswords because the domain configuration and the client use it.

     `bin/add-user.sh -a -u appone -p app++123 --role AppOne --silent`



Build and access the Quickstart
-------------------------

_NOTE: The following build command assumes you have configured your Maven user settings. If you have not, you must include Maven setting arguments on the command line. See [Build and Deploy the Quickstarts](../README.md#buildanddeploy) for complete instructions and additional options._

1. Make sure you have started and configured the JBoss Server successful as described above.
2. Open a command line and navigate to the root directory of this quickstart.
3. Type this command to build the artifacts:

        mvn clean package



Access the jboss-properties applications
---------------------

The different sub-projects in jboss-properties show how the client can be configured with the jboss-ejb-client.properties
file.

  To run a client change to the sub-project directory and type this command

       `mvn exec:exec`


simple-clustered  -  a client which use a clustered application deployed on one or more servers
-------------------------

This example shows that the configuration contains only the initial server and the server provide a list of all servers within the cluster which are used by using the 'remote.cluster.* configuration.

_NOTE: For a real environment you should add more that one node of the cluster that the connection can be established without a single point of failure._


Access the jboss-api applications
---------------------

The different Main-classes show how the EJB-client API can be used.
Be sure that the preparations are done:

1. Make sure that the deployment are successful as described above.
2. navigate to the jboss-api root directory of this quickstart.

Access with a simple client
-------------------------

The SimpleJBossApiClient invoke the AppOne bean by using the EJB-client API without properties. Depend on the given options
one or more servers are configured within the selector and secured or unsecured methods are called.

   Type this command to run the application

        `mvn exec:java -Dexec.mainClass=org.jboss.as.quickstarts.ejb.clients.SimpleJBossApiClient`

Access in a multi thread environment
-------------------------

The MultithreadCustomScopeClient invoke AppOne and AppTwo beans with different credentials and configurations in parallel.

   Type this command to run the application

        `mvn exec:java -Dexec.mainClass=org.jboss.as.quickstarts.ejb.clients.MultithreadCustomScopeClient`

Related to the implementation you will find the result of 4 threads similar to this:
Finished with scope : appTwoAsUser  Calls => {app2[quickuser]@master:app-twoA=20}
Finished with scope : appTwoMixedUsers  Calls => {app2[quickuser2]@master:app-twoB=14, app2[quickuser1]@master:app-twoA=6}
Finished with scope : appTwoAsUser2  Calls => {app2[quickuser2]@master:app-twoB=10}
Finished with scope : appOneClusterAsUser1  Calls => {master:app-oneA=11, master:app-oneB=9}

The output shows that the different threads call the application based on the defined scope. The balance of calls might differ in your output
as the invocation will use a Random approach to select the nodes by default.


Access the remoting-project application clients
---------------------

The different Main-classes show some use-cases how the client can access the server EJBs in different ways
Be sure that the preparations are done:

1. Make sure that the deployment are successful as described above.
2. navigate to the remoting  root directory of this quickstart.

onlyIC  -  clients which use only the properties of the InitialContext to connect to the server
-------------------------

The SimpleJBossRemoteClient will invoke an EJB four times by using the InitialContext with the remoting-project

   Type this command to run the application

        `mvn exec:java -Dexec.mainClass=org.jboss.as.quickstarts.ejb.clients.SimpleJBossRemoteClient`

The invocation will use the first server instance, if the first is down with the command

        'JBOSS_HOME/bin/jboss-cli.sh --connect --command="/host=master/server-config=app-oneA:stop"`

the output shows that the invocation will reach the other server automaticaly.

_NOTE: Notice that there is no loadbalancing, because the necessary properties can not be given in this case._


The MultiUserJBossRemoteClient will invoke an EJB with different credentials by using the InitialContext and pass different user credentials

   Type this command to run the application

        `mvn exec:java -Dexec.mainClass=org.jboss.as.quickstarts.ejb.clients.MultiUserJBossRemoteClient`

The output will show that the EJB will be invoked with different usernames.


Access the ic-properties applications
---------------------

The different clients show the behaviour of the new client feature 'scoped context', introduced by the feature request EJBCLIENT-34 and implemented 
in AS7.2.

_NOTE: You have to use the server and client library version 7.2 or higher of JBossAS._

The different Main-classes show the behavior of the scoped InitialContext lookup.
Be sure that the preparations are done:

1. Make sure that the installation and deployment of the multi-server quickstart is successful as described above.
2. navigate to the ic-properties root directory of this quickstart.

The MultithreadClient will run a single application with two threads where each Thread will access a different server and check whether
the response is not of a different one.

   Type this command to run the application

        `mvn exec:java -Dexec.mainClass=org.jboss.as.quickstarts.ejb.clients.MultithreadClient`
        
The output of the client will show you that the applications are invoked as expected on different servers:

Server call statistic : {app1[anonymous]@master:app-one=10}
Server call statistic : {app1[anonymous]@master:app-oneA=10}

          The allowed number of nodes in the threads are called

The statistic shows that one thread receive 10 times the answer String 'app1[anonymous]@master:app-one' which mean
that the application one is called at the server with the node.name 'master:app-one'.
The second thread receive 10 times the answer from the server with node.name 'master:app-oneA' which has the same
application deployed.
Also the message 'The allowed number of nodes in the threads are called' show that all invocations are as expected.

In the logfiles of the different servers you might follow the invocations on server-side.

_NOTE: If the feature is not used the different calles get mixed because the ejb-client is not divided in scopes and the application is invoked 
by using the identifier (ejb:app/module/Bean!View). In this test the application is deployed on both servers app-one and app-oneA._



The MultiContentClient use the scoped ejb-client-context to have different proxies for the same application (bean) and invoke these proxies
in the same thread multiple times and check whether the answer respond the correct server name or one of the expected server-names if it
is expected for loadbalancing.

   Type this command to run the application

        `mvn exec:java -Dexec.mainClass=org.jboss.as.quickstarts.ejb.clients.MultiContentClient`
        
        The output of the client will show you that the applications are invoked as expected on different servers:
        
          Call successfully reached the server 'master:app-one'
          Call successfully reached the server 'master:app-oneA'

        Also messages if a invocation can be reach different server:

          Call successfully reached the server 'master:app-one' out of [master:app-one, master:app-oneA]
          Call successfully reached the server 'master:app-oneA' out of [master:app-one, master:app-oneA]

        In the logfiles of the different servers you might follow the invocations on server-side.



Run the Quickstart in JBoss Developer Studio or Eclipse
-------------------------------------
You can also start the server and deploy the quickstarts from Eclipse using JBoss tools. For more information, see [Use JBoss Developer Studio or Eclipse to Run the Quickstarts](../README.md#useeclipse) 

Debug the Application
------------------------------------

If you want to debug the source code or look at the Javadocs of any library in the project, run either of the following commands to pull them into your local repository. The IDE should then detect them.

    mvn dependency:sources
    mvn dependency:resolve -Dclassifier=javadoc

