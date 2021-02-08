/*
 *  UnitTest: PortableMediaCreatorPluginTest.java
 *
 *  Copyright (c) 2005 by
 *
 *  IFTM Institut für Telematik in der Medizn GmbH
 *  VISUS Technology Transfer GmbH
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *
 *****************************************************************************/
package de.iftm.dcm4che.router.plugin;

import junit.framework.*;
import de.iftm.dcm4che.router.property.*;
import de.iftm.dcm4che.router.util.*;
import org.apache.log4j.*;
import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * JUnit based test for the class: PortableMediaCreatorPlugin.java
 * <br>
 * @author Thomas Hacklaender
 * @version 2006.11.17
 */
public class PortableMediaCreatorPluginTest extends TestCase {
    
    /** A link to a MR DICOM file */
    final String MR_IMAGE_URI = "./samples/MR_example.dcm";
    
    /** A link to a CT DICOM file */
    final String CT_IMAGE_URI = "./samples/CT_example.dcm";
    
    /** A link to a CR DICOM file */
    final String CR_IMAGE_URI = "./samples/CR_example.dcm";
    
    
    /** The class to test */
    PortableMediaCreatorPlugin  portableMediaCreatorPlugin = new PortableMediaCreatorPlugin();
    
    /** The logger for this plugin */
    Logger log = Logger.getLogger(PortableMediaCreatorPluginTest.class);
    
    
    public PortableMediaCreatorPluginTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Alle bestehenden Appender loeschen und Logging Level auf INFO setzen
        BasicConfigurator.resetConfiguration();
        
        // Logging System initialisieren
        BasicConfigurator.configure();
        
        // Logging Level auf INFO setzen
        Logger.getRootLogger().setLevel(Level.INFO);
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(PortableMediaCreatorPluginTest.class);
        
        return suite;
    }
    
    
    /**
     * Test of init method, of class de.iftm.dcm4che.router.plugin.PortableMediaCreatorPlugin.
     */
    public void testInitProperties() {
        Properties props = new Properties();
        
        System.out.println("testInitProperties");
        
        props.setProperty("readme-file", "./tmp/test/README.TXT");
        portableMediaCreatorPlugin.init(props);
        assertEquals(true, portableMediaCreatorPlugin.useReadmeFile);
        assertEquals(false, portableMediaCreatorPlugin.defaultReadmeFile);
        
        props.setProperty("readme-file", "default");
        portableMediaCreatorPlugin.init(props);
        assertEquals(true, portableMediaCreatorPlugin.useReadmeFile);
        assertEquals(true, portableMediaCreatorPlugin.defaultReadmeFile);
        
        props.setProperty("readme-file", "");
        portableMediaCreatorPlugin.init(props);
        assertEquals(false, portableMediaCreatorPlugin.useReadmeFile);
        assertEquals(true, portableMediaCreatorPlugin.defaultReadmeFile);
    }
    
    
    /**
     * Test of setDynamicProperties method, of class de.iftm.dcm4che.router.plugin.PortableMediaCreatorPlugin.
     */
    public void testSetDynamicProperties() {
        System.out.println("testSetDynamicProperties");
    }
    
    
    /**
     * Test structure of XHTML documents in resource.
     */
    public void testXMLResourceDocuments() {
        System.out.println("testXMLResourceDocuments");
        
        // Versucht die XHTML Resource Dateien als XML Files einzulesen
        System.out.println("Read study_index.htm");
        portableMediaCreatorPlugin.readXMLFromStream(PortableMediaCreatorPlugin.class.getResourceAsStream("resources/study_index.htm"));
        System.out.println("Read series_index.htm");
        portableMediaCreatorPlugin.readXMLFromStream(PortableMediaCreatorPlugin.class.getResourceAsStream("resources/series_index.htm"));
        System.out.println("Read image_index.htm");
        portableMediaCreatorPlugin.readXMLFromStream(PortableMediaCreatorPlugin.class.getResourceAsStream("resources/image_index.htm"));
    }
    
    
    /**
     * Test of abort method, of class de.iftm.dcm4che.router.plugin.PortableMediaCreatorPlugin.
     */
    public void testAbort() {
        System.out.println("testAbort");
    }
    
    
    /**
     * Test of close method, of class de.iftm.dcm4che.router.plugin.PortableMediaCreatorPlugin.
     */
    public void testClose() {
        System.out.println("testClose");
    }
    
    
    /**
     * Test of process method, of class de.iftm.dcm4che.router.plugin.PortableMediaCreatorPlugin.
     */
    public void testProcess() {
        File    baseDirectory = null;
        Dataset dataset = null;
        
        System.out.println("testProcess");
        
        // Das Base Directory festlegen
        portableMediaCreatorPlugin.directory = new File("./test/tmp/");
        
        try {
            // Erzeugt ein Dataset aus einer DICOM Bild-Datei
            dataset = Util.uriToDataset("./samples/tree/CT1.dcm");
            portableMediaCreatorPlugin.process(dataset);
            
            // Erzeugt ein Dataset aus einer DICOM Bild-Datei
            dataset = Util.uriToDataset("./samples/tree/MR2.dcm");
            portableMediaCreatorPlugin.process(dataset);
            
            // Erzeugt ein Dataset aus einer DICOM Bild-Datei
            dataset = Util.uriToDataset("./samples/tree/MR1.dcm");
            portableMediaCreatorPlugin.process(dataset);
            
            // Erzeugt ein Dataset aus einer DICOM Bild-Datei
            dataset = Util.uriToDataset("./samples/tree/MR3.dcm");
            portableMediaCreatorPlugin.process(dataset);
        } catch (IOException e) {
            fail("Cant read URI");
        }
        
    }
    
    
    /**
     * Test of linkToFile method, of class de.iftm.dcm4che.router.plugin.PortableMediaCreatorPlugin.
     */
    public void testLinkToFile() {
        String s;
        
        System.out.println("testLinkToFile");
        
        s = portableMediaCreatorPlugin.linkToFile(new File("test/"), new File("test/a.txt"));
        assertEquals("a.txt", s);
        
        s = portableMediaCreatorPlugin.linkToFile(new File("test/b.txt"), new File("test/a.txt"));
        assertEquals("a.txt", s);
        
        s = portableMediaCreatorPlugin.linkToFile(new File("test/"), new File("test/a/b/c/d.txt"));
        assertEquals("a/b/c/d.txt", s);
    }
    
    
    /**
     * Test sorting of table rows.
     */
    public void testTableSort() {
        Document document;
        Element tableElement;
        
        PortableMediaCreatorPlugin.HTMLTable htmlTable;
        PortableMediaCreatorPlugin.HTMLTableRow htmlTableRow;
        String[] columnValues = {"columne1", "columne2"};
        
        System.out.println("testTableSort");
        
        document = createEmptyDocument();
        // tr Knoten erzeugen
        tableElement = document.createElement("table");
        tableElement.setAttribute("id", "patient");
        document.appendChild(tableElement);
        htmlTable = portableMediaCreatorPlugin.newInstanceOfHTMLTable(document, "patient");
        
        htmlTableRow = portableMediaCreatorPlugin.newInstanceOfHTMLTableRow(document, columnValues, "id1", "sort2", null);
        htmlTable.append(htmlTableRow);
        
        htmlTableRow = portableMediaCreatorPlugin.newInstanceOfHTMLTableRow(document, columnValues, "id2", "sort3", null);
        htmlTable.append(htmlTableRow);
        
        htmlTableRow = portableMediaCreatorPlugin.newInstanceOfHTMLTableRow(document, columnValues, "id3", "sort1", null);
        htmlTable.append(htmlTableRow);
        
        htmlTable.sort();
        
        assertEquals("sort1", ((PortableMediaCreatorPlugin.HTMLTableRow) htmlTable.tableRows.get(0)).titleAttrValue);
        assertEquals("sort2", ((PortableMediaCreatorPlugin.HTMLTableRow) htmlTable.tableRows.get(1)).titleAttrValue);
        assertEquals("sort3", ((PortableMediaCreatorPlugin.HTMLTableRow) htmlTable.tableRows.get(2)).titleAttrValue);
    }
    
    
    /**
     * Test HTML file read/write methods
     */
    public void testReadWriteHTMLDocument() {
        File baseDirectory = null;
        Dataset dataset = null;
        Document document = null;
        
        System.out.println("testReadWriteHTMLDocument");
        
        document = portableMediaCreatorPlugin.readXMLFromFile(new File("./test/cfg/html/image_index.htm"));
        portableMediaCreatorPlugin.writeXMLToFile(document, new File("./test/tmp/image_index_out.htm"));
        
        // Lokale Methode in PortableMediaCreatorPluginTest
        // System.out.println(writeToString(document));
    }
    
    
    /**
     * Create an empty DOM document
     *
     * @return the DOM document.
     */
    private Document createEmptyDocument() {
        DocumentBuilder builder;
        Document document = null;
        
        // Create a factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        // Ignore comments
        factory.setIgnoringComments(true);
        // Convert CDATA to Text nodes
        factory.setCoalescing(true);
        // No Namespace
        factory.setNamespaceAware(false);
        // Don't validate DTD
        factory.setValidating(false);
        
        try{
            // Create document builder
            builder = factory.newDocumentBuilder();
            // Create document
            document = builder.newDocument();
        } catch (ParserConfigurationException e) {
            log.error("createEmptyDocument::ParserConfigurationException: " + e);
        }
        
        return document;
    }
    
    
    /**
     * Write a DOM document in a XML (XHTML) representation to a string.
     *
     * @param document  the document to write.
     * @return the string to write to.
     */
    private  String writeXMLToString(Document document) {
        StringWriter sw = new StringWriter();
        
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(document);
            //Prepare the output string
            Result result = new StreamResult(sw);
            //Write DOM document to file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            // Wird method="html" gewaelt, dann fuegt der Transformer den Tag
            // <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
            // zwischen <head> und </head> ein. Dieser Tag ist nicht XML konform
            // (es fehlt </META> und er ist in Grossbuchstabengeschrieben) und
            // fuehrt beim erneuten Einlesen zu einer SAX Exception.
            xformer.setOutputProperty(OutputKeys.METHOD, "xml");
            xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.0 Strict//EN");
            xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
            xformer.transform(source, result);
            
        } catch (TransformerConfigurationException e){
            log.error("writeToFile::TransformerConfigurationException: " + e);
        } catch (TransformerException e) {
            log.error("writeToFile::TransformerException: " + e);
        }
        
        return sw.toString();
    }
    
}
