/*
 * XMLUtils.java
 *
 * Created on December 6, 2001, 1:21 PM
 */
package org.realtor.rets.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * Utilities for dealing with XML
 * @author  tweber
 * @version 1.0
 */
public class XMLUtils {
    /**  This method dumps out a dom document to an output stream.
     *   Header information is turned off.
     *
     *   @param doc Dom Document
     *   @param os existing outputstream you wish to write the document to.
     */
    public static void DOMtoOutputStream(Document doc, OutputStream os) {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource src = new DOMSource(doc);
            StreamResult result = new StreamResult(os);

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            //transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(src, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Does an xsl tranformation of an XML document and returns a String result.
     *
     * @param xmlDoc string value of an xmlDocument
     * @param xslFile filename of an XSL file.
     */
    public static String transformXmlToString(String xmlDoc, String xslFile,
        Map parameters) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformXml(xmlDoc, xslFile, baos, parameters);

            return baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String transformXmlToString(String xmlDoc, String xslFile) {
        return transformXmlToString(xmlDoc, xslFile, null);
    }

    public static void transformXml(String xmlDoc, String xslFile,
        OutputStream os) {
        transformXml(xmlDoc, xslFile, os, null);
    }

    /** Does an xsl tranformation of an XML document and writes the result.
     *  to an output stream.
     *
     * @param xmlDoc string value of an xmlDocument
     * @param xslFile filename of an XSL file.
     * @param os  OutputStream to write the results to.
     */
    public static void transformXml(String xmlDoc, String xslFile,
        OutputStream os, Map parameters) {
        try {
            File stylesheet = new File(xslFile);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlDoc.getBytes());

            String urlString= "file:"+ResourceLocator.locate("dummy.dtd");
            System.out.println("URL String:"+urlString);
            Document document = builder.parse(bais,urlString);

            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            StreamSource stylesource = new StreamSource(stylesheet);
            Transformer transformer = tFactory.newTransformer(stylesource);

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(os);

            if (parameters != null) {
                Iterator iter = parameters.keySet().iterator();

                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    transformer.setParameter(key, parameters.get(key));
                }
            }

            transformer.transform(source, result);
        } catch (TransformerConfigurationException tce) {
            // Error generated by the parser
            System.out.println("\n** Transformer Factory error");
            System.out.println("   " + tce.getMessage());

            // Use the contained exception, if any
            Throwable x = tce;

            if (tce.getException() != null) {
                x = tce.getException();
            }

            x.printStackTrace();
        } catch (TransformerException te) {
            // Error generated by the parser
            System.out.println("\n** Transformation error");
            System.out.println("   " + te.getMessage());

            // Use the contained exception, if any
            Throwable x = te;

            if (te.getException() != null) {
                x = te.getException();
            }

            x.printStackTrace();
        } catch (Exception e) {
            // error
            e.printStackTrace();
        }
    }

    /**  Creates an Element and sets the text value of the element. Appends the
     *  element to rootNode.
     *
     * @param doc DOM Document
     * @param rootNode node to add an element to
     * @param elementName name of the new element
     * @param elementValue value of the new element.
     */
    public static void addTextElement2Node(Document doc, Node rootNode,
        String elementName, String elementValue) {
        try {
            Element element = doc.createElement(elementName);
            Node text = doc.createTextNode(elementValue);
            element.appendChild(text);

            //doc.appendChild(element);
            rootNode.appendChild(element);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static NodeList executeXpathQuery(Node root, String query)
        throws TransformerException {
        
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr = xpath.compile(query);

            Object result = expr.evaluate(root, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            return nodes;
        }
        catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void printNodeList(NodeList list) {
        for (int i = 0; i < list.getLength(); i++) {
            printNode(list.item(i), "");
            System.out.println("############################");
        }
    }

    public static void printNode(Node node, String indent) {
        String nodeName = node.getNodeName();
        System.out.print(indent);

        if (nodeName.equals("#text")) {
            String nodeText = node.getNodeValue();

            if ((nodeText != null) && (nodeText.trim().length() > 0)) {
                System.out.print(nodeText.trim());
            }
        } else {
            System.out.print(nodeName);
        }

        System.out.print(" ");

        if (!nodeName.equals("#text")) {
            NamedNodeMap attrs = node.getAttributes();

            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    System.out.print(attr.getNodeName() + "=\"" +
                        attr.getNodeValue() + "\" ");
                }
            }
        }

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            System.out.println();
            printNode(children.item(i), indent + "\t");
        }
    }

    public static Document stringToDocument(String xml) {
        Document doc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Turn on validation, and turn off namespaces
        factory.setValidating(false);
        factory.setNamespaceAware(false);

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }
}