/*
 *  UnitTest: ExportFilesystemPluginTest.java
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
 * JUnit based test for the class: ExportImagePluginT.java
 * <br>
 * @author Thomas Hacklaender
 * @version 2006.11.17
 */
public class ExportImagePluginTest  extends TestCase {
    
    /** A link to a MR DICOM file */
    final String MR_IMAGE_URI = "./samples/MR_example.dcm";
    
    /** A link to a CT DICOM file */
    final String CT_IMAGE_URI = "./samples/CT_example.dcm";
    
    /** A link to a CR DICOM file */
    final String CR_IMAGE_URI = "./samples/CR_example.dcm";
    
    /** The class to test */
    ExportImagePlugin exportImagePlugin = null;
    
    /** The frame to display the image */
    JFrame f;
    
    /**
     * Creates a new ExportImagePluginTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public ExportImagePluginTest(java.lang.String testName) {
        super(testName);
    }
    
    
    /**
     * Create a new test suite based on this class.
     *
     * @return The test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ExportImagePluginTest.class);
        
        return suite;
    }
    
    
    /**
     * Setup procedures. This method is called once during initialization.
     */
    protected void setUp() {
        // Alle bestehenden Appender loeschen und Logging Level auf INFO setzen
        BasicConfigurator.resetConfiguration();
        
        // Logging System initialisieren
        BasicConfigurator.configure();
        
        // Logging Level auf INFO setzen
        Logger.getRootLogger().setLevel(Level.INFO);
        
        // Eine Instanz des Plugin erzeugen
        exportImagePlugin = new ExportImagePlugin();
        
        // Leere Properties fuer des Plugin setzen
        exportImagePlugin.init(new Properties());
    }
    
    
    /**
     * Cleanup procedures. This method is called once before finishing the test suite.
     */
    protected void tearDown() {
        // Nichts zu tun
    }
    
    
    /**
     * Test of process method, of class
     * de.iftm.dcm4che.router.plugin.ExportImagePlugin.
     */
    public void testProcess() {
        Dataset    ds = null;
        JFrame     f;
        Annotation annotation;
        
        System.out.println("testProcess");
        
        // Erzeugt ein Dataset aus einer DICOM Bild-Datei
        try {
            ds = Util.uriToDataset(MR_IMAGE_URI);
        } catch (IOException e) {
            fail("Cant read URI");
        }
        
        // Erzeugt ein BufferedImage aus dem Dataset
        // exportImagePlugin.imageSize = 256;
        exportImagePlugin.dataset2Image(ds);
        
        // Annotationen-Objekt erzeugen
        annotation = new Annotation(exportImagePlugin.image, ds);
        
        // annotation.setType (Annotation.ANNOTATION_NO);
        // annotation.setType (Annotation.ANNOTATION_FULL);
        // annotation.setType (Annotation.ANNOTATION_MINIMAL);
        // annotation.setType (Annotation.ANNOTATION_ANONYMOUS);
        // annotation.setType (Annotation.ANNOTATION_PSEUDONYM);
        // annotation.setFontColor (Color.green);
        // annotation.setFontName ("monospaced");
        // annotation.setFontSize (24);
        // annotation.setMarginSize (8);
        // Annotation schreiben
        annotation.draw();
        
        // Das ferige Bild in einem eigenen Frame anzeigen. Die VM des Unit-Tests
        // wird erst dann beendet, wenn dieser Frame durch den Benutzer geschlossen
        // wird.
        displayImage(exportImagePlugin.image);
    }
    
    
    /**
     * Test of init method, of class
     * de.iftm.dcm4che.router.plugin.ExportImagePlugin.
     */
    public void testInitProperties() {
        Dataset    ds = null;
        Properties props;
        String     key;
        String     value;
        
        System.out.println("initProperties");
        
        // Erzeugt ein Dataset aus einer DICOM Bild-Datei
        try {
            ds = Util.uriToDataset(MR_IMAGE_URI);
        } catch (IOException e) {
            fail("Cant read URI");
        }
        
        props = new Properties();
        
        key = "name-postfix";
        value = "_org";
        props.setProperty(key, value);
        exportImagePlugin.init(props);
        assertEquals(key + " = " + value, "_org", exportImagePlugin.name_postfix);
        
        key = "image-size";
        value = "original";
        props.setProperty(key, value);
        exportImagePlugin.init(props);
        assertEquals(key + " = " + value, true, exportImagePlugin.originalSize);
        
        key = "image-size";
        value = "512";
        props.setProperty(key, value);
        exportImagePlugin.init(props);
        assertEquals(key + " = " + value, false, exportImagePlugin.originalSize);
        assertEquals(key + " = " + value, 512, exportImagePlugin.image_size);
    }
    
    
    /**
     * Test of createFilePathToSave method, of class
     * de.iftm.dcm4che.router.plugin.ExportImagePlugin.
     */
    public void testCreateFilePathToSave() {
        Dataset ds;
        File    f;
        File    parent;
        
        System.out.println("createFileObject");
        
        // Leeres Dataset erzeugen
        ds = DcmObjectFactory.getInstance().newDataset();
        
        // Character Set für Westeuropa ISO-8859-1 festlegen
        // Anderenfalls wuerden deutsche Umlaute zu einer IllegalArgumentException
        // beim Befuellen des Dataset fuehren
        ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
        
        ds.putPN(Tags.PatientName, "Mustermann^Hans Werner^von^Dr.");
        ds.putDA(Tags.PatientBirthDate, "19570522");
        ds.putDA(Tags.StudyDate, "20041230");
        ds.putTM(Tags.StudyTime, "153342.763");
        ds.putCS(Tags.Modality, "MR");
        ds.putSH(Tags.StudyID, "4711");
        ds.putIS(Tags.SeriesNumber, "2");
        ds.putIS(Tags.InstanceNumber, "54");
        
        parent = new File("test/tmp/");
        
        exportImagePlugin.directory = parent;
        exportImagePlugin.image_format = "jpg";
        exportImagePlugin.filename = "MyFile";
        exportImagePlugin.name_postfix = "";
        exportImagePlugin.use_subdirectory = "";
        exportImagePlugin.write_dir_tree = false;
        f = exportImagePlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
        
        exportImagePlugin.directory = parent;
        exportImagePlugin.image_format = "jpg";
        exportImagePlugin.filename = "MyFile";
        exportImagePlugin.name_postfix = "4711";
        exportImagePlugin.use_subdirectory = "";
        exportImagePlugin.write_dir_tree = false;
        f = exportImagePlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
        
        exportImagePlugin.directory = parent;
        exportImagePlugin.image_format = "jpg";
        exportImagePlugin.filename = "MyFile";
        exportImagePlugin.name_postfix = "4711";
        exportImagePlugin.use_subdirectory = "IHE";
        exportImagePlugin.write_dir_tree = false;
        f = exportImagePlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
        
        exportImagePlugin.directory = parent;
        exportImagePlugin.use_subdirectory = "IHE";
        exportImagePlugin.write_dir_tree = true;
        f = exportImagePlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
        
        exportImagePlugin.directory = parent;
        exportImagePlugin.use_subdirectory = "IHE";
        exportImagePlugin.write_dir_tree = true;
        parent = Util.addPatientDirectory(parent, ds);
        f = exportImagePlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
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
