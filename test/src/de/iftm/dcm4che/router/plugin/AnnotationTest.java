/*
 *  UnitTest: Annotation.java
 *
 *  Copyright (c) 2003 by
 *
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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.imageio.*;

import javax.swing.*;


/**
 * JUnit based test for the class: Annotation.java
 * <br>
 * @author Thomas Hacklaender
 * @version 2006.11.17
 */
public class AnnotationTest extends TestCase {
    
     /** A link to a MR DICOM file */
    final String MR_IMAGE_URI = "./samples/MR_example.dcm";
    
    /** A link to a CT DICOM file */
    final String CT_IMAGE_URI = "./samples/CT_example.dcm";
    
    /** A link to a CR DICOM file */
    final String CR_IMAGE_URI = "./samples/CR_example.dcm";
    
    /** The class to test */
    Annotation annotationClass = null;
    
    /** A Dataset */
    Dataset dataset = null;
    
    /** A BufferedImage */
    BufferedImage image = null;
    
    /** The frame to display the image */
    JFrame f;
    
    /**
     * Creates a new AnnotationTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public AnnotationTest(java.lang.String testName) {
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
        TestSuite suite = new TestSuite(AnnotationTest.class);
        
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
        
        // Erzeugt ein Dataset aus einer DICOM Bild-Datei
        try {
            dataset = Util.uriToDataset(MR_IMAGE_URI);
        } catch (IOException e) {
            fail("Cant read MR_IMAGE_URI");
            return;
        }
        
        // Erzeugt ein leeres BufferedImage
        image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        
        // Eine Instanz des Plugin erzeugen
        annotationClass = new Annotation(image, dataset);
    }
    
    
    /**
     * Cleanup procedures.
     * This method is called once before finishing the test suite.
     */
    protected void tearDown() {
        // Nichts zu tun
    }
    
    
    /**
     * Test of formatDouble method, of class
     * de.iftm.dcm4che.router.plugin.annotationClass.
     */
    public void testFormatDouble() {
        String s;
        
        System.out.println("formatDouble");
        
        s = "null";
        assertEquals(s, "", annotationClass.formatDouble(s, 0));
        
        s = "";
        assertEquals(s, "", annotationClass.formatDouble(s, 0));
        
        s = "TEXT";
        assertEquals(s, "", annotationClass.formatDouble(s, 0));
        
        s = "12.30456";
        assertEquals(s, "12", annotationClass.formatDouble(s, 0));
        assertEquals(s, "12.3", annotationClass.formatDouble(s, 1));
        
        // Rundung!!
        assertEquals(s, "12.305", annotationClass.formatDouble(s, 3));
        
        s = "0.1230456E02";
        assertEquals(s, "12", annotationClass.formatDouble(s, 0));
        assertEquals(s, "12.3", annotationClass.formatDouble(s, 1));
        
        // Rundung!!
        assertEquals(s, "12.305", annotationClass.formatDouble(s, 3));
    }
    
    
    /**
     * Test of string2LetterDigit method, of class
     * de.iftm.dcm4che.router.plugin.annotationClass.
     */
    public void testString2LetterDigit() {
        String s;
        
        System.out.println("string2LetterDigit");
        
        s = "Mustermann^Hans Werner^von^Dr.";
        assertEquals(s, "Mustermann_Hans_Werner_von_Dr_", annotationClass.string2LetterDigit(s));
        
        s = "Hackländer^Thomas";
        assertEquals(s, "Hackl_nder_Thomas", annotationClass.string2LetterDigit(s));
    }
    
    
    /**
     * Test of createPseudonym method, of class
     * de.iftm.dcm4che.router.plugin.annotationClass.
     */
    public void testCreatePseudonym() {
        Dataset    ds;
        PersonName pn;
        String     s;
        
        System.out.println("createPseudonym");
        
        // Leeres Dataset erzeugen
        ds = DcmObjectFactory.getInstance().newDataset();
        
        // Character Set für Westeuropa ISO-8859-1 festlegen
        // Anderenfalls wuerden deutsche Umlaute zu einer IllegalArgumentException
        // beim Befuellen des Dataset fuehren
        ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
        
        ds.putPN(Tags.PatientName, "Mustermann^Hans Werner^von^Dr.");
        s = annotationClass.createPseudonym(ds);
        assertEquals(s, "MH_000101", s);
        
        ds.putPN(Tags.PatientName, "Mustermann^Hans Werner^von^Dr.");
        ds.putDA(Tags.PatientBirthDate, "19401201");
        s = annotationClass.createPseudonym(ds);
        assertEquals(s, "MH_401201", s);
        
        ds.putPN(Tags.PatientName, "Özedir^Yildrim");
        ds.putDA(Tags.PatientBirthDate, "19401201");
        s = annotationClass.createPseudonym(ds);
        assertEquals(s, "ÖY_401201", s);
        
        ds.putPN(Tags.PatientName, "Mustermann");
        ds.putDA(Tags.PatientBirthDate, "19401201");
        s = annotationClass.createPseudonym(ds);
        assertEquals(s, "M__401201", s);
    }
    
    
    /**
     * Displays the BufferedImage ExportImagePlugin#bi in a JFrame.
     *
     * @param bi The BufferedImage to display.
     */
    private void displayImage(BufferedImage bi) {
        JLabel label;
        JPanel panel;
        
        f = new JFrame("Datset2ImageTest");
        
        label = new JLabel(new ImageIcon(bi));
        panel = new JPanel();
        panel.add(label);
        f.getContentPane().add(panel);
        
        f.pack();
        f.setVisible(true);
        
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                f.dispose();
            }
        });
    }
        
}
