/*
 *  UnitTest: UtilTest.java
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
package de.iftm.dcm4che.router.util;

import junit.framework.*;

import org.apache.log4j.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;

import java.io.*;


/**
 * JUnit based test for the class: Implementation.java
 * <br>
 * @author Thomas Hacklaender
 * @version 2005.01.27
 */
public class UtilTest extends TestCase {
    
    /** The class to test */
    Util util = null;
    
    public UtilTest(String testName) {
        super(testName);
    }

    
    protected void setUp() throws java.lang.Exception {
        // Alle bestehenden Appender loeschen und Logging Level auf INFO setzen
        BasicConfigurator.resetConfiguration ();

        // Logging System initialisieren
        BasicConfigurator.configure ();

        // Logging Level auf INFO setzen
        Logger.getRootLogger ().setLevel (Level.INFO);
    }

    
    protected void tearDown() throws java.lang.Exception {
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(UtilTest.class);
        
        return suite;
    }

    
    /**
     * Test of getTagFromProperty method, of class de.iftm.dcm4che.router.util.Util.
     */
    public void testGetTagFromProperty() {
        String s;
        
        System.out.println("testGetTagFromProperty");
        
        // PatientID = (0010, 0020)
        
        s = "Test";
        assertEquals(0, util.getTagFromPropertyString(s));
        
        s = "$PatientID";
        assertEquals(0x00100020, util.getTagFromPropertyString(s));
        
        s = "$Test";
        assertEquals(-1, util.getTagFromPropertyString(s));
        
        s = "@00100020";
        assertEquals(0x00100020, util.getTagFromPropertyString(s));
        
        s = "@AAAABBBB";
        assertEquals(-1, util.getTagFromPropertyString(s));
    }

    
    /**
     * Test of getTagStringOrValue method, of class de.iftm.dcm4che.router.util.Util.
     */
    public void testGetTagStringOrValue() {
        Dataset    ds;
        String      s;
        
        System.out.println("testGetTagStringOrValue");
        
        // Leeres Dataset erzeugen
        ds = DcmObjectFactory.getInstance().newDataset();
        
        // Character Set für Westeuropa ISO-8859-1 festlegen
        // Anderenfalls wuerden deutsche Umlaute zu einer IllegalArgumentException
        // beim Befuellen des Dataset fuehren
        ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
        
        ds.putPN(Tags.PatientName, "Mustermann^Hans Werner^von^Dr.");
        ds.putLO(Tags.PatientID, "4711");
        ds.putDA(Tags.StudyDate, "20041230");
        
        s = "Test";
        assertEquals("Test", util.getTagStringOrValue(s, ds));
        
        s = "$StudyDate";
        assertEquals("20041230", util.getTagStringOrValue(s, ds));
        
        // PatientID = (0010, 0020)
        
        s = "$PatientID";
        assertEquals("4711", util.getTagStringOrValue(s, ds));
        
        s = "@00100020";
        assertEquals("4711", util.getTagStringOrValue(s, ds));
    }

    
    /**
     * Test of datasetToDicomPath method, of class de.iftm.dcm4che.router.util.Util.
     */
    public void testDatasetToTreeFileID() {
        Dataset    ds;
        String      s;
        
        System.out.println("testDatasetToDicomPath");
        
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
        s = util.datasetToTreeFileID(ds);
        assertEquals(s, "MH570522/041230/MR1533/4711/2/54", s);
        
        ds.putPN(Tags.PatientName, "Ülle^Hans");
        s = util.datasetToTreeFileID(ds);
        assertEquals(s, "_H570522/041230/MR1533/4711/2/54", s);
        
    }
    
}
