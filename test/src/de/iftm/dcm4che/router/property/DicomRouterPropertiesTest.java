/*
 *  UnitTest: DicomRouterPropertiesTest.java
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
package de.iftm.dcm4che.router.property;

import junit.framework.*;

import org.apache.log4j.*;

import org.dcm4che.data.*;

import java.io.*;

import java.net.*;


/**
 * JUnit based test for the class: PropertyString.java
 * <br>
 * @author Thomas Hacklaender
 * @version 2004.02.15
 */
public class DicomRouterPropertiesTest
        extends TestCase {
    
    /**
     * Creates a new PropertyStringTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public DicomRouterPropertiesTest(java.lang.String testName) {
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
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(DicomRouterPropertiesTest.class);
        
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
    }
    
    
    /**
     * Cleanup procedures.
     * This method is called once before finishing the test suite.
     */
    protected void tearDown() {
        // Nichts zu tun
    }
    
    public void testLoadPropertiesFromStream() {
        DicomRouterProperties drp = null;
        InputStream in = null;
        
        System.out.println("testLoadPropertiesFromStream");
        
        drp = new DicomRouterProperties();
        in = DicomRouterPropertiesTest.class.getResourceAsStream("resources/test.cfg");
        drp.loadLogged(in);
        
        assertEquals(drp.getProperty("max-clients"), "10");
        assertEquals(drp.getProperty("pc.HardcopyGrayscaleImageStorage"), "$ts-image");
        assertEquals(drp.getProperty("dir.PATIENT.SpecificCharacterSet"), "");
    }
    
    /**
     * Test of uriToFile method, of class
     * de.iftm.dcm4che.router.property.DicomRouterProperties.
     */
    public void testUriToFile() {
        File f;
        
        System.out.println("testUriToFile");
        
        f = DicomRouterProperties.uriToFile("file:/c:/user/tom/foo.txt");
        assertEquals("c:\\user\\tom\\foo.txt", f.getPath());
    }
}
