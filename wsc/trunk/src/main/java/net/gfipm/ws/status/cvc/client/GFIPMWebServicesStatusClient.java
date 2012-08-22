/*
 * Copyright (c) 2012, Georgia Institute of Technology. All Rights Reserved.
 * This code was developed by Georgia Tech Research Institute (GTRI) under
 * a grant from the U.S. Dept. of Justice, Bureau of Justice Assistance.
 */
package net.gfipm.ws.status.cvc.client;

import com.sun.xml.ws.developer.StreamingDataHandler;
import net.gfipm.trustfabric.TrustFabric;
import net.gfipm.trustfabric.TrustFabricFactory;
import net.gfipm.ws.status.cvc.jaxb.msg.*;
import net.gfipm.ws.status.cvc.jaxb.iepd.*;
import net.gfipm.ws.status.cvc.jaxws.GFIPMWebServicesStatusPortType;
import net.gfipm.ws.status.cvc.jaxws.GFIPMWebServicesStatusWebService;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.bind.Marshaller;


public class GFIPMWebServicesStatusClient {

    //see WSIT tutorial for detals : http://docs.sun.com/app/docs/doc/820-1072/6ncp48v40?a=view#ahicy
    //or https://jax-ws.dev.java.net/guide/HTTPS_HostnameVerifier.html
//    static {
//        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
//                new javax.net.ssl.HostnameVerifier() {
//
//                    @Override
//                    public boolean verify(String hostname,
//                            javax.net.ssl.SSLSession sslSession) {
//                        System.out.println("Veryfing hostname: " + hostname);
////                        if (hostname.equals("xwssecurityserver")) {
////                            return true;
////                        }
////                        return false;
//                        return true;
//                    }
//                });
//
//        //http://java.sun.com/javase/javaseforbusiness/docs/TLSReadme.html
//        //java.lang.System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
//    }
    //.NET Alias:  ha50wsp
//    private static String sepUrl = "https://ha50wspm1:8443/Model1/CommercialVehicleCollisionPortType.svc"
    //Metro Alias:  curewsp

//    private static String wsdlUrl = "https://wsp.ref.gfipm.net/wspStatus/services/cvc?wsdl";
//    private static String sepUrl = "https://wsp.ref.gfipm.net/wspStatus/services/cvc";

    private static TrustFabric tf;

    //NOTE: modify main/resources/META-INF/CommercialVehicleCollisionWebserviceIntf.xml to use proper Trust certificates/stores for aliases.
    public static void execute(GFIPMStatusWSCCommandLineArguments cli) throws MalformedURLException, Exception {

        GFIPMWebServicesStatusPortType     cvcPort;
        GFIPMWebServicesStatusWebService   cvcWebService;
       
        String strTrustLocation = cli.getMetadataUrl(); 
        tf = TrustFabricFactory.getInstance(strTrustLocation);

        String wsdlUrl;
        String sepUrl;
        String entityId;

        // Read the wsdlUrl and sepUrl from trust fabric based on either a URL or Entity ID as input
        if ( cli.getWspUrl() != null )
        {
           sepUrl   = cli.getWspUrl();
           entityId = tf.getEntityIdBySEP(sepUrl);
          
           if ( ! tf.isWebServiceProvider(entityId) ) 
           {
              System.out.println("EntityId " + entityId + " is not marked as a Web Service Provider in the Trust Fabric\n" );
              return;
           }

           wsdlUrl = tf.getWsdlUrlAddress(entityId);
         } 
         else if ( cli.getEntityId() != null ) 
         {
           entityId = cli.getEntityId();
           if ( ! tf.isWebServiceProvider(entityId) )
           {
              System.out.println("EntityId " + entityId + " is not marked as a Web Service Provider in the Trust Fabric\n" );
              return;
           }

           sepUrl  = tf.getWebServiceEndpointAddress(entityId);
           wsdlUrl = tf.getWsdlUrlAddress(entityId);
        } 
        else 
        {
          // The CLI validation should prevent this state from ever occurring.
          System.out.println("No WSP specified by input\n" );
          return;
        }

        System.out.println ("Querying Status Service for EntityId (" + entityId + "):");
        System.out.println ("   SEP URL: " + sepUrl);
 
        cvcWebService = new GFIPMWebServicesStatusWebService(
                new URL(wsdlUrl),
                new QName("http://gfipm.net/system/status/ws/wsdl",
                "GFIPMWebServicesStatusWebService"));
        cvcPort = cvcWebService.getGFIPMWebServicesStatusPort(); 
        Map<String, Object> requestContext = ((BindingProvider) cvcPort).getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, sepUrl);
        System.out.println("Using following SEP: " + requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
        //It is possible to add schema validation through Service feature / include ErrorHandler from the server side to the library
//        WebServiceFeature feature = new SchemaValidationFeature(gov.niem.ws.sample.jaxwsspr.server.handler.ErrorHandler.class);
//        cvcPort = new CommercialVehicleCollisionWebService().getCommercialVehicleCollisionPort(feature);

        net.gfipm.ws.status.cvc.jaxb.msg.ObjectFactory  msgOF  = new net.gfipm.ws.status.cvc.jaxb.msg.ObjectFactory();
        net.gfipm.ws.status.cvc.jaxb.iepd.ObjectFactory iepdOF = new net.gfipm.ws.status.cvc.jaxb.iepd.ObjectFactory();

        // Build Service Request
        GetSystemStatusRequestType getSystemStatusType = msgOF.createGetSystemStatusRequestType();
        JAXBElement<String> systemName = iepdOF.createSystemName("system-name-to-echo");
        getSystemStatusType.setSystemName(systemName);
        
        // Invoke Service
        GetSystemStatusResponseType getStatusResponseType = cvcPort.getSystemStatus (getSystemStatusType);

        // Output the Results to the Console
        SystemStatusType systemStatus = getStatusResponseType.getSystemStatus().getValue();
        System.out.println("---------------------------------------------------------");
        System.out.println("System Name: " + systemStatus.getSystemName().getValue() );
        System.out.println("  Description: " + systemStatus.getDescription().getValue() );
        System.out.println("  Status: " + systemStatus.getStatus().getValue() ); 
        System.out.println("---------------------------------------------------------");

        // Output the raw XML Status
        JAXBContext jc = JAXBContext.newInstance( "net.gfipm.ws.status.cvc.jaxb.iepd" );
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(new JAXBElement<SystemStatusType>(new QName("http://gfipm.net/system/status/0.3","SystemStatus"), SystemStatusType.class, systemStatus), System.out);
        System.out.println("---------------------------------------------------------");
    }

    public static void main(String[] args) throws Exception {
        GFIPMStatusWSCCommandLineArguments cli = new GFIPMStatusWSCCommandLineArguments (args);
        cli.parseCommandLineArguments(args);


        //Logs
        if (false) {
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
            System.setProperty("com.sun.xml.ws.assembler.jaxws.TerminalTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.assembler.jaxws.HandlerTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.assembler.jaxws.ValidationTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.assembler.jaxws.MustUnderstandTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.assembler.jaxws.MonitoringTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.assembler.jaxws.AddressingTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.tx.runtime.TxTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.rx.rm.runtime.RmTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.rx.mc.runtime.McTubeFactory", "true");
            System.setProperty("com.sun.xml.wss.provider.wsit.SecurityTubeFactory", "true");//enable this to check messages
            System.setProperty("com.sun.xml.ws.dump.ActionDumpTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.rx.testing.PacketFilteringTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.dump.MessageDumpingTubeFactory", "true");
            System.setProperty("com.sun.xml.ws.assembler.jaxws.TransportTubeFactory", "true");
        }

        String currentDirAbsolutePath = System.getProperty("user.dir");
        System.setProperty("javax.net.ssl.keyStore", currentDirAbsolutePath + cli.getTruststore() );
        System.setProperty("javax.net.ssl.keyStorePassword", cli.getKeystorePassword() );
        System.setProperty("javax.net.ssl.trustStore", currentDirAbsolutePath + cli.getTruststore() );
        System.setProperty("javax.net.ssl.trustStorePassword", cli.getTruststorePassword() );

        if ( cli.doHelp() )
           cli.printHelp(System.out);
        else
           GFIPMWebServicesStatusClient.execute(cli);

    }
}
