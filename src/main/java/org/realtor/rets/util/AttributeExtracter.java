/*
 * AttributeExtracter.java
 *
 * Created on October 28, 2002, 3:50 PM
 */
package org.realtor.rets.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;


/**
 *
 * @author  tweber
 */
public class AttributeExtracter extends DefaultHandler {
    //private String delim = "  ";
    //private StringBuffer textBuffer;
    private String currentElement = null;
    HashMap<String, HashMap<String, String>> retHash = new HashMap<String, HashMap<String, String>>();

    /** Creates a new instance of AttributeExtracter */
    public AttributeExtracter() {
    }

    public void startElement(String namespaceURI, String sName, // simple name
        String qName, // qualified name
        Attributes attrs) throws SAXException {
        //textBuffer = null;

        //String eName = qName; // element name
        currentElement = qName;

        if (("DATA".equalsIgnoreCase(currentElement)) ||
                ("COLUMNS".equalsIgnoreCase(currentElement))) {
            return;
        }

        HashMap<String, String> attHash = new HashMap<String, String>();

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name

                if ("".equals(aName)) {
                    aName = attrs.getQName(i);
                }

                attHash.put(aName.toUpperCase(), attrs.getValue(aName));
            }
        }

        retHash.put(currentElement.toUpperCase(), attHash);
    }

    public HashMap<String, HashMap<String, String>> getHash() {
        return retHash;
    }
}
