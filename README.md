gfipm-ws-status
===============

  This is a GFIPM Web Services Status System.  It is a java tool that uses Metro for 
WS-* support and is built with maven. 

  There are three main projects:
   - wscontract - This is the Web Services Contract that represents the web service.  
       It specifies the WSDL interface and generates a good bit of automated code for use 
       in the service and consumer.  This must be built first.

   - wsc - This is the web service consumer.  It is a command line java program, typically
       invoked via an included shell script.  'mvn clean install' will build a zipfile that
       includes the script file and all necessary files for invocation.

   - wsp - This is the web service provider.  Maven will build a war file that can be deployed
       to Glassfish.  Do be aware that Glassfish requires extensive configuration to operate
       properly.


