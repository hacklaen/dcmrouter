/*
 *  XSLTransformPluginTest.java
 *
 *  Copyright (c) 2003 by
 *
 *  VISUS Technology Transfer GmbH
 *  IFTM Institut für Telematik in der Medizn GmbH
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

import de.iftm.dcm4che.router.property.*;
import de.iftm.dcm4che.router.util.*;

import junit.framework.*;

import org.apache.log4j.*;

import org.dcm4che.data.*;

import org.dcm4che.dict.*;

import java.io.*;

import java.net.*;

import java.util.*;


/**
 * JUnit based test for the class: xslTransformPlugin.java
 * <br><br>
 * 2004.12.29, hacklaender:
 * stopTag Parameter durch untouch_pixel_data Parameter ersetzt.
 *
 * @author Thomas Hacklaender
 * @version 2006.11.17
 */
public class XSLTransformPluginTest extends TestCase {
    
    /** A link to a MR DICOM file */
    final String MR_IMAGE_URI = "./samples/MR_example.dcm";
    
    /** A link to a CT DICOM file */
    final String CT_IMAGE_URI = "./samples/CT_example.dcm";
    
    /** A link to a CR DICOM file */
    final String CR_IMAGE_URI = "./samples/CR_example.dcm";

    /** A link to the XSL directory */
    final String XSL_DIRECTORY_URI = "./test/cfg/xsl/";
    
    /** A link to the output directory */
    final String OUT_DIRECTORY_URI = "./test/tmp/";
    
    /** The class to test */
    XSLTransformPlugin xslTransformPlugin = null;
    
    /**
     * Creates a new TransformXSLPluginTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public XSLTransformPluginTest(java.lang.String testName) {
        super(testName);
    }
    
    /**
     * Main method to run the test suite.
     *
     * @param args The argument strings.
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    
    /**
     * Create a new test suite based on this class.
     *
     * @return The test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(XSLTransformPluginTest.class);
        
        return suite;
    }
    
    
    /**
     * Setup procedures.
     * This method is called once during initialization.
     */
    protected void setUp() {
        // Alle bestehenden Appender loeschen und Logging Level auf INFO setzen
        BasicConfigurator.resetConfiguration();
        
        // Logging System initialisieren
        BasicConfigurator.configure();
        
        // Logging Level auf INFO setzen
        Logger.getRootLogger().setLevel(Level.INFO);
        
        // Eine Instanz des Plugin erzeugen
        xslTransformPlugin = new XSLTransformPlugin();
        
        // Leere Properties fuer des Plugin setzten
        xslTransformPlugin.init(new Properties());
    }
    
    
    /**
     * Cleanup procedures.
     * This method is called once before finishing the test suite.
     */
    protected void tearDown() {
        // Nichts zu tun
    }
    
    
    /**
     * Test of init method,
     * of class de.iftm.dcm4che.router.plugin.xslTransformPlugin.
     */
    public void testInitProperties() {
        String value;
        String contents;
        Properties props = new Properties();
        
        System.out.println("testInitProperties");
        
        // Die "xxx_directory" werden nicht getestet, da das Ergebnis abhaengig
        // vom Betriebssystem des Rechners ist, auf dem der Test läuft.
        
        value = "True";
        props.setProperty("save-source-xml", value);
        xslTransformPlugin.init(props);
        assertEquals(value, true, xslTransformPlugin.save_source_xml);
        
        value = ".txt";
        props.setProperty("source-xml-extension", value);
        xslTransformPlugin.init(props);
        assertEquals(value, value, xslTransformPlugin.source_xml_extension);
        
        value = "True";
        props.setProperty("transform", value);
        xslTransformPlugin.init(props);
        assertEquals(value, true, xslTransformPlugin.transform);
        
        value = "output";
        props.setProperty("xsl-filename", value);
        xslTransformPlugin.init(props);
        assertEquals(value, value, xslTransformPlugin.xsl_filename);
        
        value = "True";
        props.setProperty("save-transformed-xml", value);
        xslTransformPlugin.init(props);
        assertEquals(value, true, xslTransformPlugin.save_transformed_xml);
        
        value = "True";
        props.setProperty("reconvert-transformed-xml", value);
        xslTransformPlugin.init(props);
        assertEquals(value, true, xslTransformPlugin.reconvert_transformed_xml);
        
        value = ".txt";
        props.setProperty("transformed-xml-extension", value);
        xslTransformPlugin.init(props);
        assertEquals(value, value, xslTransformPlugin.transformed_xml_extension);
    }
    
    
    /**
     * Test of setDynamicProperties method,
     * of class de.iftm.dcm4che.router.plugin.xslTransformPlugin.
     */
    public void testSetDynamicProperties() {
        Dataset ds;
        String value;
        String contents;
        Properties props = null;
        
        System.out.println("testSetDynamicProperties");
        
        // Lokale Properties fuer die Testklasse setzen
        props = new Properties();
        xslTransformPlugin.init(props);
        
        // Leeres Dataset erzeugen
        ds = DcmObjectFactory.getInstance().newDataset();
        
        // Character Set für Westeuropa ISO-8859-1 festlegen
        // Anderenfalls wuerden deutsche Umlaute zu einer IllegalArgumentException
        // beim Befuellen des Dataset fuehren
        ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
        
        // Test fuer einen gegebenen Source-Namen
        value = "source";
        props.setProperty("source-xml-filename", value);
        xslTransformPlugin.setDynamicProperties(ds);
        assertEquals(value, value, xslTransformPlugin.source_xml_filename);
        
        // Test fuer einen Source-Namen aus einen DICOM Element
        value = "$SOPInstanceUID";
        props.setProperty("source-xml-filename", value);
        contents = "1.2.3.4";
        ds.putUI(Tags.SOPInstanceUID, contents);
        xslTransformPlugin.setDynamicProperties(ds);
        assertEquals(value, contents, xslTransformPlugin.source_xml_filename);
        
        // Test fuer einen gegebenen Transformed-Namen
        value = "transformed";
        props.setProperty("transformed-xml-filename", value);
        xslTransformPlugin.setDynamicProperties(ds);
        assertEquals(value, value, xslTransformPlugin.transformed_xml_filename);
        
        // Test fuer einen Source-Namen aus einen DICOM Element
        value = "$PatientBirthDate";
        props.setProperty("transformed-xml-filename", value);
        contents = "19401201";
        ds.putDA(Tags.PatientBirthDate, contents);
        xslTransformPlugin.setDynamicProperties(ds);
        assertEquals(value, contents, xslTransformPlugin.transformed_xml_filename);
    }
    
    
    /**
     * Test of datasetToXML method,
     * of class de.iftm.dcm4che.router.plugin.xslTransformPlugin.
     */
    public void testDatasetToXML() {
        Dataset ds = null;
        
        System.out.println("testDatasetToXML");
        
        // Erzeugt ein Dataset aus einer DICOM Bild-Datei
        try {
            ds = Util.uriToDataset(MR_IMAGE_URI);
        } catch (IOException e) {
            fail("Cant read URI");
        }
        
        // Property-Fields setzen
        xslTransformPlugin.save_source_xml = true;
        xslTransformPlugin.source_xml_directory = Util.uriToFile(OUT_DIRECTORY_URI);
        
        this.assertNotNull(xslTransformPlugin.datasetToXML(ds));
    }
    
    
    /**
     * Test of transformXML method,
     * of class de.iftm.dcm4che.router.plugin.xslTransformPlugin.
     */
    public void testTransformXML() {
        Dataset               ds = null;
        ByteArrayOutputStream sourceXmlOS = null;
        ByteArrayInputStream  sourceXmlIS = null;
        
        System.out.println("testTransformXML");
        
        // Erzeugt ein Dataset aus einer DICOM Bild-Datei
        try {
            ds = Util.uriToDataset(MR_IMAGE_URI);
        } catch (IOException e) {
            fail("Cant read URI");
        }
        
        // XML Repraesentation erzeugen
        sourceXmlOS = xslTransformPlugin.datasetToXML(ds);
        sourceXmlIS = new ByteArrayInputStream(sourceXmlOS.toByteArray());
        
        this.assertNotNull(xslTransformPlugin.transformXML(sourceXmlIS));
    }
    
    
    /**
     * Test of xmlToDataset method,
     * of class de.iftm.dcm4che.router.plugin.xslTransformPlugin.
     */
    public void testXmlToDataset() {
        Dataset               sourceDS = null;
        Dataset               resultDS;
        ByteArrayOutputStream sourceXmlOS = null;
        ByteArrayInputStream  sourceXmlIS = null;
        
        System.out.println("testXmlToDataset");
        
        // Erzeugt ein Dataset aus einer DICOM Bild-Datei
        try {
            sourceDS = Util.uriToDataset(MR_IMAGE_URI);
        } catch (IOException e) {
            fail("Cant read URI");
        }
        
        // XML Repraesentation erzeugen
        sourceXmlOS = xslTransformPlugin.datasetToXML(sourceDS);
        sourceXmlIS = new ByteArrayInputStream(sourceXmlOS.toByteArray());
        
        // Zurueck transformieren
        resultDS = xslTransformPlugin.xmlToDataset(sourceXmlIS);
        
        this.assertEquals(sourceDS.size(), resultDS.size());
    }
    
    
    /**
     * Test of process method,
     * of class de.iftm.dcm4che.router.plugin.xslTransformPlugin.
     */
    public void testprocess() {
        Dataset ds = null;
        boolean success;
        
        System.out.println("testprocess");
        
        // Erzeugt ein Dataset aus einer DICOM Bild-Datei
        try {
            ds = Util.uriToDataset(MR_IMAGE_URI);
        } catch (IOException e) {
            fail("Cant read URI");
        }
        
        // Dataset ohne Pixeldaten in XML umwandeln
        xslTransformPlugin.untouch_pixel_data = true;
        
        // Source speichern
        xslTransformPlugin.save_source_xml = true;
        xslTransformPlugin.source_xml_directory = Util.uriToFile(OUT_DIRECTORY_URI);
        xslTransformPlugin.source_xml_filename = "JunitTest_source";
        xslTransformPlugin.source_xml_extension = ".xml";
        
        // Nicht transformieren
        xslTransformPlugin.transform = false;
        assertEquals(DicomRouterPlugin.CONTINUE, xslTransformPlugin.process(ds));
        
        // Dataset ohne Pixeldaten in XML umwandeln
        xslTransformPlugin.untouch_pixel_data = true;
        
        // Source nicht speichern
        xslTransformPlugin.save_source_xml = false;
        
        // Umwandlung in ein HTML Dokument
        xslTransformPlugin.transform = true;
        xslTransformPlugin.xsl_directory = Util.uriToFile(XSL_DIRECTORY_URI);
        xslTransformPlugin.xsl_filename = "ToHTML.xsl";
        
        // Transformation speichern
        xslTransformPlugin.save_transformed_xml = true;
        xslTransformPlugin.transformed_xml_directory = Util.uriToFile(OUT_DIRECTORY_URI);
        xslTransformPlugin.transformed_xml_filename = "JunitTest_transformed";
        xslTransformPlugin.transformed_xml_extension = ".html";
        
        // Nicht Rueckwandeln
        xslTransformPlugin.reconvert_transformed_xml = false;
        
        assertEquals(DicomRouterPlugin.CONTINUE, xslTransformPlugin.process(ds));
        
        // Dataset mit Pixeldaten in XML umwandeln
        xslTransformPlugin.untouch_pixel_data = false;
        
        // Source nicht speichern
        xslTransformPlugin.save_source_xml = false;
        
        // Identitäts-Transformation
        xslTransformPlugin.transform = true;
        xslTransformPlugin.xsl_directory = Util.uriToFile(XSL_DIRECTORY_URI);
        xslTransformPlugin.xsl_filename = "Identity.xsl";
        
        // Transformation speichern
        xslTransformPlugin.save_transformed_xml = true;
        xslTransformPlugin.transformed_xml_directory = Util.uriToFile(OUT_DIRECTORY_URI);
        xslTransformPlugin.transformed_xml_filename = "JunitTest_transformed";
        xslTransformPlugin.transformed_xml_extension = ".xml";
        
        // Rueckwandeln
        xslTransformPlugin.reconvert_transformed_xml = true;
        
        assertEquals(DicomRouterPlugin.CONTINUE, xslTransformPlugin.process(ds));
        
    }
}
