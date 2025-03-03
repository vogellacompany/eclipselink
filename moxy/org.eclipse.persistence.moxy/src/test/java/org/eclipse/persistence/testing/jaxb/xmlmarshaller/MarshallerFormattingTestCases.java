/*
 * Copyright (c) 1998, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.testing.jaxb.xmlmarshaller;

import java.io.StringWriter;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.eclipse.persistence.testing.oxm.OXTestCase;

public class MarshallerFormattingTestCases extends OXTestCase {
    private final static int CONTROL_EMPLOYEE_ID = 123;
    private final static String CRLF = System.getProperty("line.separator");
    private final static String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private final static String XML_BODY = "<employee><id>123</id><name>Bob</name><phone>123456789</phone></employee>";
    private final static String XML_BODY_FORMATTED = "<employee>" + CRLF + "   <id>123</id>" + CRLF + "   <name>Bob</name>" + CRLF + "   <phone>123456789</phone>" + CRLF + "</employee>" + CRLF;
    private Marshaller marshaller;
    private Object controlObject;
    private String contextPath;

    public MarshallerFormattingTestCases(String name) {
        super(name);
    }

    public static void main(String[] args) {
        //System.setProperty("useDeploymentXML", "true");
        //System.setProperty("useDocPres", "true");
        //System.setProperty("useLogging", "true");
        //System.setProperty("useSAXParsing", "true");
        String arguments[] = { "-c", "org.eclipse.persistence.testing.oxm.jaxb.MarshallerFormattingTestCases" };
        TestRunner.main(arguments);
    }

    @Override
    public void setUp() throws Exception {
        contextPath = System.getProperty("jaxb.test.contextpath", JAXBSAXTestSuite.CONTEXT_PATH);
        JAXBContext jaxbContext = JAXBContext.newInstance(contextPath, getClass().getClassLoader());
        marshaller = jaxbContext.createMarshaller();
        controlObject = setupControlObject();
    }

    protected Employee setupControlObject() {
        Employee employee = new Employee();
        employee.setID(CONTROL_EMPLOYEE_ID);
        employee.setName("Bob");
        Phone p = new Phone();
        p.setNumber("123456789");
        employee.setPhone(p);
        return employee;
    }

    public void testFormattingFalse() throws Exception {
        formatTest(false, XML_HEADER + XML_BODY);
    }

    public void testFormattingTrue() throws Exception {
        formatTest(true, XML_HEADER  + CRLF + XML_BODY_FORMATTED);
    }

    public void testInvalidFormatting() throws Exception {
        try {
            Object value = 10;
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, value);
        } catch (PropertyException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            fail("The wrong exception occurred, should have been a PropertyException" + e.getMessage());
            return;
        }
        fail("A PropertyException should have been thrown but wasn't");
    }

    private void formatTest(boolean isFormatted, String controlString) throws Exception {
        StringWriter writer = new StringWriter();
        Boolean originalSetting = (Boolean) marshaller.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT);

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, isFormatted);
        marshaller.marshal(controlObject, writer);

        log("Expected:" + controlString);
        log("Actual  :" + writer.toString());

        assertTrue(controlString.equals(writer.toString()));

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, originalSetting);
    }
}
