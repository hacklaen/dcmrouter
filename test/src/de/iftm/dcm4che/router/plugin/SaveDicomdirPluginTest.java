/*
 *  UnitTest: SaveDicomdirPluginTest.java
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

import org.apache.log4j.*;

import de.iftm.dcm4che.router.property.*;
import de.iftm.dcm4che.router.util.*;
import org.apache.log4j.*;
import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.media.*;
import org.dcm4che.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * JUnit based test for the class: SaveDicomdirPlugin.java
 * <br>
 * @author Thomas Hacklaender
 * @version 2006.11.17
 */
public class SaveDicomdirPluginTest extends TestCase {
    
    /** The class to test */
    SaveDicomdirPlugin  saveDicomdirPlugin = null;
    
    
    public SaveDicomdirPluginTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Alle bestehenden Appender loeschen und Logging Level auf INFO setzen
        BasicConfigurator.resetConfiguration();
        
        // Logging System initialisieren
        BasicConfigurator.configure();
        
        // Logging Level auf INFO setzen
        Logger.getRootLogger().setLevel(Level.INFO);
        
        // Instanz von SaveDicomdirPlugin erzeugen und mit leeren Properties initialisieren
        saveDicomdirPlugin = new SaveDicomdirPlugin();
        saveDicomdirPlugin.init (null);
    }
    
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(SaveDicomdirPluginTest.class);
        
        return suite;
    }
    
    /**
     * Test of resource file containig properties.
     */
    public void testConfigResource() {
        String key;
        String value;
        
        System.out.println("testConfigResource");
        
        // Alle Properties listen
        // saveDicomdirPlugin.cfg.list(System.out);
        
        key = "dir.PATIENT.PatientName";
        assertEquals("Key " + key, true, saveDicomdirPlugin.cfg.containsKey(key));
        
        key = "dir.STUDY.StudyDate";
        assertEquals("Key " + key, true, saveDicomdirPlugin.cfg.containsKey(key));
        
        key = "dir.SERIES.SeriesDate";
        assertEquals("Key " + key, true, saveDicomdirPlugin.cfg.containsKey(key));
    }
    
    /**
     * Test of resource file containig properties.
     */
    public void testDirBuilderPreferences() {
        int tag;
        Dataset ds;
        
        System.out.println("testDirBuilderPreferences");
        
        tag = Tags.PatientName;
        ds = saveDicomdirPlugin.dirBuilderPref.getFilterForRecordType(DirRecord.PATIENT);
        assertEquals("Tag " + Tags.toString(tag), true, ds.contains(tag));
        
        tag = Tags.InstanceNumber;
        ds = saveDicomdirPlugin.dirBuilderPref.getFilterForRecordType(DirRecord.IMAGE);
        assertEquals("Tag " + Tags.toString(tag), true, ds.contains(tag));
    }


    /**
     * Test of init method
     */
    public void testInitProperties () {
        Dataset    ds;
        Properties props;
        String     key;
        String     value;

        System.out.println ("initProperties");
        
        // Dataset erzeugen
        ds = DcmObjectFactory.getInstance().newDataset();
        // Character Set für Westeuropa ISO-8859-1 festlegen
        // Anderenfalls wuerden deutsche Umlaute zu einer IllegalArgumentException
        // beim Befuellen des Dataset fuehren
        ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
        ds.putPN(Tags.PatientName, "Mustermann^Hans Werner^von^Dr.");
        ds.putDA(Tags.PatientBirthDate, "19570522");

        props = new Properties();

        key = "use-subdirectory";
        value = "DICOM";
        props.setProperty (key, value);
        saveDicomdirPlugin.init (props);
        assertEquals (key + " = " + value, value, saveDicomdirPlugin.use_subdirectory);

        key = "use-subdirectory";
        value = "";
        props.setProperty (key, value);
        saveDicomdirPlugin.init (props);
        assertEquals (key + " = " + value, value, saveDicomdirPlugin.use_subdirectory);
    }
    
    /**
     * Test of randomFileID method, of class de.iftm.dcm4che.router.plugin.SaveDicomdirPlugin.
     */
    public void testRandomFileID() {
        System.out.println("testRandomFileID");
        
        System.out.println(saveDicomdirPlugin.randomFileID());
    }
    
    /**
     * Test of randomUniqueFile method, of class de.iftm.dcm4che.router.plugin.SaveDicomdirPlugin.
     */
    public void testRandomUniqueFile() {
        String s;
        File f;
        
        System.out.println("testRandomUniqueFile");
        
        // Set properties
        saveDicomdirPlugin.directory = new File("test/tmp/");
        
        saveDicomdirPlugin.directory.mkdirs();
        System.out.println(saveDicomdirPlugin.directory.getAbsolutePath());
        
        try {
            s = saveDicomdirPlugin.randomUniqueFileID(saveDicomdirPlugin.directory);
            f = new File(saveDicomdirPlugin.directory, s);
            f.createNewFile();
            System.out.println(s);
            
            s = saveDicomdirPlugin.randomUniqueFileID(saveDicomdirPlugin.directory);
            f = new File(saveDicomdirPlugin.directory, s);
            f.createNewFile();
            System.out.println(s);
            
            s = saveDicomdirPlugin.randomUniqueFileID(saveDicomdirPlugin.directory);
            f = new File(saveDicomdirPlugin.directory, s);
            f.createNewFile();
            System.out.println(s);
        } catch (IOException e) {
            fail("IO Error: " + e);
        }
    }
    
    /**
     * Test of createFilePathToSave method, of class de.iftm.dcm4che.router.plugin.SaveDicomdirPlugin.
     */
    public void testCreateFilePathToSave() {
        Dataset     ds = null;
        File        parent = null;
        File        f;
        String      s;
        
        System.out.println("testCreateFilePathToSave");
        
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
        
        // Set properties
        saveDicomdirPlugin.directory = parent;
        saveDicomdirPlugin.use_subdirectory = "";
        saveDicomdirPlugin.write_dir_tree = false;
        f = saveDicomdirPlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
        
        // Set properties
        saveDicomdirPlugin.directory = parent;
        saveDicomdirPlugin.use_subdirectory = "DICOM";
        saveDicomdirPlugin.write_dir_tree = false;
        f = saveDicomdirPlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
        
        // Set properties
        saveDicomdirPlugin.directory = parent;
        saveDicomdirPlugin.use_subdirectory = "DICOM";
        saveDicomdirPlugin.write_dir_tree = true;
        f = saveDicomdirPlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
        
        // Set properties
        saveDicomdirPlugin.directory = parent;
        saveDicomdirPlugin.use_subdirectory = "DICOM";
        saveDicomdirPlugin.write_dir_tree = true;
        parent = Util.addPatientDirectory(parent, ds);
        f = saveDicomdirPlugin.createFilePathToSave(parent, ds);
        System.out.println(f.getPath());
    }
}
