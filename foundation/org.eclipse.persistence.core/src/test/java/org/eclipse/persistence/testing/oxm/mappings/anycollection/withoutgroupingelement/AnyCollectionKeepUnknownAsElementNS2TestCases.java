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
package org.eclipse.persistence.testing.oxm.mappings.anycollection.withoutgroupingelement;


/**
 *  @version $Header: AnyCollectionKeepUnknownAsElementTestCases.java 30-jul-2007.15:34:52 dmccann Exp $
 *  @author  mmacivor
 *  @since   release specific (what release of product did this appear in)
 */
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.textui.TestRunner;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.eclipse.persistence.testing.oxm.mappings.XMLMappingTestCases;

public class AnyCollectionKeepUnknownAsElementNS2TestCases extends XMLMappingTestCases {
    private static String XML_CHILD_ELEMENTS = "org/eclipse/persistence/testing/oxm/mappings/anycollection/withoutgroupingelement/child_elements_minus_child_ns2.xml";

    public AnyCollectionKeepUnknownAsElementNS2TestCases(String name) throws Exception {
        super(name);
        setProject(new AnyCollectionWithoutGroupingElementKeepUnkownAsElementProject());
        setControlDocument("org/eclipse/persistence/testing/oxm/mappings/anycollection/withoutgroupingelement/keep_as_element_ns2.xml");
    }

    @Override
    public Object getControlObject() {
        Root root = new Root();
        Vector any = new Vector();
        Child child = new Child();
        child.setContent("Child1");
        any.add(child);
        root.setAny(any);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(getClass().getClassLoader().getResourceAsStream(XML_CHILD_ELEMENTS));
            Element rootElem = doc.getDocumentElement();
            NodeList children = rootElem.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Element.ELEMENT_NODE) {
                    any.add(children.item(i));
                }
            }
        } catch (Exception ex) {
        }

        return root;
    }

    public static void main(String[] args) {
        String[] arguments = { "-c", "org.eclipse.persistence.testing.oxm.mappings.anycollection.withoutgroupingelement.AnyCollectionKeepUnknownAsElementNS2TestCases" };
        TestRunner.main(arguments);
    }

    @Override
    public void xmlToObjectTest(Object testObject) throws Exception {
        super.xmlToObjectTest(testObject);
        Object firstObject = ((Root)testObject).getAny().get(1);
        assertTrue(firstObject instanceof Element);
        assertEquals("foo:element1", ((Element)firstObject).getNodeName());
        assertEquals("uri1", ((Element)firstObject).getNamespaceURI());

        Attr nsDecl = ((Element)firstObject).getAttributeNode("xmlns:" + "foo");
        assertNotNull(nsDecl);
        assertEquals("uri1", nsDecl.getValue());

    }

    @Override
    public Document getWriteControlDocument() throws Exception
    {

       java.io.InputStream inputStream = ClassLoader.getSystemResourceAsStream("org/eclipse/persistence/testing/oxm/mappings/anycollection/withoutgroupingelement/keep_as_element_ns2_write.xml");

        Document writeControlDocument = parser.parse(inputStream);
        removeEmptyTextNodes(controlDocument);
        inputStream.close();
        return writeControlDocument;
    }
}
