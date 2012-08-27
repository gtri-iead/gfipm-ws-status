/*
 * Copyright (c) 2012, Georgia Institute of Technology. All Rights Reserved.
 * This code was developed by Georgia Tech Research Institute (GTRI) under
 * a grant from the U.S. Dept. of Justice, Bureau of Justice Assistance.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.gfipm.ws.status.cvc.service;

import net.gfipm.ws.status.cvc.jaxb.iepd.SystemStatusType;
import net.gfipm.ws.status.cvc.jaxb.msg.*;
import net.gfipm.ws.status.cvc.jaxws.GFIPMWebServicesStatusPortType;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;

@MTOM
@WebService(serviceName = "GFIPMWebServicesStatusWebService",
portName = "GFIPMWebServicesStatusPort",
endpointInterface = "net.gfipm.ws.status.cvc.jaxws.GFIPMWebServicesStatusPortType",
targetNamespace = "http://gfipm.net/system/status/ws/wsdl",
wsdlLocation = "WEB-INF/wsdl/GFIPMWebServicesStatusImpl.wsdl")
public class GFIPMWebServicesStatusWebServiceImpl implements GFIPMWebServicesStatusPortType {

    static String strStatusXmlFile = "/opt/gfipmstatus/status.xml";

    net.gfipm.ws.status.cvc.jaxb.msg.ObjectFactory  msgOF  = new net.gfipm.ws.status.cvc.jaxb.msg.ObjectFactory();
    net.gfipm.ws.status.cvc.jaxb.iepd.ObjectFactory iepdOF = new net.gfipm.ws.status.cvc.jaxb.iepd.ObjectFactory();
    int imageCounter = 0;
    @Resource
    WebServiceContext wsContext;

    @Override
    public GetSystemStatusResponseType getSystemStatus(GetSystemStatusRequestType parameters) {
        
        String currentMethodName = GFIPMAuthorizationProvider.getCurrentMethodName();
        if( ! GFIPMAuthorizationProvider.isServiceAuthorized(currentMethodName, wsContext) ){
            throw new RuntimeException("WSC is not authorized to invoke " + currentMethodName + " method");
        }        

        String systemStatusIDString = parameters.getSystemName().getValue();
        GetSystemStatusResponseType getSystemStatusResponse = msgOF.createGetSystemStatusResponseType();

        try {
            JAXBContext jcu = JAXBContext.newInstance("net.gfipm.ws.status.cvc.jaxb");
            Unmarshaller u = jcu.createUnmarshaller();
            JAXBElement rootElement = (JAXBElement) u.unmarshal( new FileInputStream( strStatusXmlFile ) );
            SystemStatusType gfipmSystemStatus = (SystemStatusType) rootElement.getValue ();

            // Build System Status XML Document
            // gfipmSystemStatus.setSystemName(iepdOF.createSystemName(systemStatusIDString));
            // gfipmSystemStatus.setDescription(iepdOF.createDescription("A Description of the system or subsystem"));
            // gfipmSystemStatus.setStatus(iepdOF.createStatus("OK"));

            // Set the Status Document as the Response
            getSystemStatusResponse.setSystemStatus(iepdOF.createSystemStatus(gfipmSystemStatus));

            JAXBContext jc = JAXBContext.newInstance("net.gfipm.ws.status.cvc.jaxb");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(msgOF.createGetSystemStatusResponse(getSystemStatusResponse), System.out);
        } catch (JAXBException ex) {
            Logger.getLogger(GFIPMWebServicesStatusWebServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception fex) {
            Logger.getLogger(GFIPMWebServicesStatusWebServiceImpl.class.getName()).log(Level.SEVERE, null, fex);
        }

        return getSystemStatusResponse;
    }

    private static DataHandler getDataHandler(final int total) {
        return new DataHandler(new DataSource() {

            @Override
            public InputStream getInputStream() throws IOException {
                return new InputStream() {

                    int i;

                    @Override
                    public int read() throws IOException {
                        return i < total ? 'A' + (i++ % 26) : -1;
                    }
                };
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return null;
            }

            @Override
            public String getContentType() {
                return "application/octet-stream";
            }

            @Override
            public String getName() {
                return "";
            }
        });
    }
}
