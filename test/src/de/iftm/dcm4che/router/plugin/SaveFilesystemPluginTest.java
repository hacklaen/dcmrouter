/*
 *  UnitTest: SaveFilesystemPluginTest.java
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
 * JUnit based test for the class: SaveFilesystemPlugin.java
 * <br>
 * @author Thomas Hacklaender
 * @version 2006.11.17
 */
public class SaveFilesystemPluginTest
        extends TestCase {
    /** A link to a MR DICOM file */
    final String MR_IMAGE_URI = "./samples/MR_example.dcm";
    
    /** A link to a CT DICOM file */
    final String CT_IMAGE_URI = "./samples/CT_example.dcm";
    
    /** A link to a CR DICOM file */
    final String CR_IMAGE_URI = "./samples/CR_example.dcm";
    
    /** The class to test */
    SaveFilesystemPlugin saveFilesystemPlugin = null;
    
    /**
     * Creates a new SaveFilesystemPluginTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public SaveFilesystemPluginTest(java.lang.String testName) {
        super(testName);
    }
    
    
    /**
     * Create a new test suite based on this class.
     *
     * @return The test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(SaveFilesystemPluginTest.class);
        
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
        saveFilesystemPlugin = new SaveFilesystemPlugin();
        
        // Leere Properties fuer des Plugin setzten
        saveFilesystemPlugin.init(new Properties());
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
     * of class de.iftm.dcm4che.router.plugin.SaveFilesystemPlugin.
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
        key = "write-dir-tree";
        value = "false";
        props.setProperty(key, value);
        saveFilesystemPlugin.init(props);
        assertEquals(key + " = " + value, false, saveFilesystemPlugin.write_dir_tree);
    }
    
    
    /**
     * Test of uriToFile method,
     * of class de.iftm.dcm4che.router.plugin.SaveFilesystemPlugin.
     */
    public void testUriToFile() {
        File f;
        
        System.out.println("testUriToFile");
        
        f = Util.uriToFile("file:/c:/user/tom/foo.txt");
        assertEquals("c:\\user\\tom\\foo.txt", f.getPath());
    }
    
    
    /**
     * Test of createFilePathToSave method.
     */
    public void testCreateFilePathToSave() {
        Dataset ds;
        File    f;
        File    parent;
        String  s;
        
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
        
        saveFilesystemPlugin.directory = parent;
        saveFilesystemPlugin.extension = "dcm";
        saveFilesystemPlugin.filename = "MyFile";
        saveFilesystemPlugin.use_subdirectory = "";
        saveFilesystemPlugin.write_dir_tree = false;
        f = saveFilesystemPlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
        
        saveFilesystemPlugin.directory = parent;
        saveFilesystemPlugin.extension = "dcm";
        saveFilesystemPlugin.filename = "MyFile";
        saveFilesystemPlugin.use_subdirectory = "IHE";
        saveFilesystemPlugin.write_dir_tree = false;
        f = saveFilesystemPlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
        
        saveFilesystemPlugin.directory = parent;
        saveFilesystemPlugin.use_subdirectory = "IHE";
        saveFilesystemPlugin.write_dir_tree = true;
        f = saveFilesystemPlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
        
        saveFilesystemPlugin.directory = parent;
        saveFilesystemPlugin.use_subdirectory = "IHE";
        saveFilesystemPlugin.write_dir_tree = true;
        parent = Util.addPatientDirectory(parent, ds);
        f = saveFilesystemPlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
    }
}
