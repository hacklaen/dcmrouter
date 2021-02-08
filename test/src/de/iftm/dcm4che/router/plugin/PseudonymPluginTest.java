/*
 *  UnitTest: PseudonymPluginTest.java
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

import junit.framework.*;

import org.apache.log4j.*;

import org.dcm4che.data.*;

import org.dcm4che.dict.*;

import java.util.*;


/**
 * JUnit based test for the class: PseudonymPlugin.java
 * <br>
 * @author Thomas Hacklaender
 * @version 2006.11.17
 */
public class PseudonymPluginTest
    extends TestCase {
    /** A link to a MR DICOM file */
    final String MR_IMAGE_URI = "./example_images/MR_example.dcm";

    /** A link to a CT DICOM file */
    final String CT_IMAGE_URI = "./example_images/CT_example.dcm";

    /** A link to a CR DICOM file */
    final String CR_IMAGE_URI = "./example_images/CR_example.dcm";

    /** The class to test */
    PseudonymPlugin pseudonymPlugin = null;

    /**
     * Creates a new PseudonymPluginTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public PseudonymPluginTest (java.lang.String testName) {
        super(testName);
    }

    /**
     * Main method to run the test suite.
     *
     * @param args The argument strings.
     */
    public static void main (java.lang.String[] args) {
        junit.textui.TestRunner.run (suite ());
    }


    /**
     * Create a new test suite based on this class.
     *
     * @return The test suite
     */
    public static Test suite () {
        TestSuite suite = new TestSuite(PseudonymPluginTest.class);

        return suite;
    }


    /**
     * Setup procedures.
     * This method is called once during initialization.
     */
    protected void setUp () {
        // Alle bestehenden Appender loeschen und Logging Level auf INFO setzen
        BasicConfigurator.resetConfiguration ();

        // Logging System initialisieren
        BasicConfigurator.configure ();

        // Logging Level auf INFO setzen
        Logger.getRootLogger ().setLevel (Level.INFO);

        // Eine Instanz des Plugin erzeugen
        pseudonymPlugin = new PseudonymPlugin();

        // Leere Properties fuer des Plugin setzen
        pseudonymPlugin.init (new Properties());
    }


    /**
     * Cleanup procedures.
     * This method is called once before finishing the test suite.
     */
    protected void tearDown () {
        // Nichts zu tun
    }


    /**
     * Test of process method,
     * of class de.iftm.dcm4che.router.plugin.PseudonymPlugin.
     */
    public void testProcess () {
        System.out.println ("testProcess");

        // Keine Tests definiert
    }


    /**
     * Test of initials2_Birdat6 method,
     *  of class de.iftm.dcm4che.router.plugin.PseudonymPlugin.
     */
    public void testInitials2_Birdat6 () {
        Dataset ds = null;
        String  s;

        System.out.println ("testInitials2_Birdat6");

        // Leeres Dataset erzeugen
        ds = DcmObjectFactory.getInstance ().newDataset ();

        // Character Set für Westeuropa ISO-8859-1 festlegen
        // Anderenfalls wuerden deutsche Umlaute zu einer IllegalArgumentException
        // beim Befuellen des Dataset fuehren
        ds.putCS (Tags.SpecificCharacterSet, "ISO_IR 100");

        s = pseudonymPlugin.initials2_Birdat6 (ds);
        assertEquals (s, "XX_000101", s);

        ds.putDA (Tags.PatientBirthDate, "19401201");
        s = pseudonymPlugin.initials2_Birdat6 (ds);
        assertEquals (s, "XX_401201", s);

        ds.putPN (Tags.PatientName, "Mustermann^Hans Werner^von^Dr.");
        ds.putDA (Tags.PatientBirthDate, "19401201");
        s = pseudonymPlugin.initials2_Birdat6 (ds);
        assertEquals (s, "MH_401201", s);
    }


    /**
     * Test of mod2Acqdat6_Birdat6 method, of class de.iftm.dcm4che.router.plugin.PseudonymPlugin.
     */
    public void testMod2Acqdat6_Birdat6 () {
        Dataset ds = null;
        String  s;

        System.out.println ("testMod2Acqdat8_Birdat8");

        // Leeres Dataset erzeugen
        ds = DcmObjectFactory.getInstance ().newDataset ();

        // Character Set für Westeuropa ISO-8859-1 festlegen
        // Anderenfalls wuerden deutsche Umlaute zu einer IllegalArgumentException
        // beim Befuellen des Dataset fuehren
        ds.putCS (Tags.SpecificCharacterSet, "ISO_IR 100");

        s = pseudonymPlugin.mod2Acqdat8_Birdat8 (ds);
        assertEquals (s, "XX20030101_20000101", s);

        ds.putDA (Tags.PatientBirthDate, "19401201");
        s = pseudonymPlugin.mod2Acqdat8_Birdat8 (ds);
        assertEquals (s, "XX20030101_19401201", s);

        ds.putPN (Tags.Modality, "CT");
        ds.putDA (Tags.PatientBirthDate, "19401201");
        ds.putDA (Tags.AcquisitionDate, "20030731");
        s = pseudonymPlugin.mod2Acqdat8_Birdat8 (ds);
        assertEquals (s, "CT20030731_19401201", s);
    }
}
