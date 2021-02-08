/*
 *  UnitTest: PropertyString.java
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

import org.dcm4che.dict.*;

import java.io.*;

import java.net.*;


/**
 * JUnit based test for the class: PropertyString.java
 * <br>
 * @author Thomas Hacklaender
 * @version 2004.02.08
 */
public class PropertyStringTest
    extends TestCase {

    /**
     * Creates a new PropertyStringTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public PropertyStringTest (java.lang.String testName) {
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
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Test suite () {
        TestSuite suite = new TestSuite(PropertyStringTest.class);

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
    }


    /**
     * Cleanup procedures.
     * This method is called once before finishing the test suite.
     */
    protected void tearDown () {
        // Nichts zu tun
    }


    /**
     * Test of isCommand method, of class
     * de.iftm.dcm4che.router.property.PropertyString.
     */
    public void testIsCLeadIn () {
        PropertyString propertyString = null;
        String         s;

        System.out.println ("testIsLeadIn");

        s = "$test";
        propertyString = new PropertyString(s);
        assertEquals (s, true, propertyString.isLeadIn ('$'));

        s = "$$test";
        propertyString = new PropertyString(s);
        assertEquals (s, false, propertyString.isLeadIn ('$'));

        s = "$#test";
        propertyString = new PropertyString(s);
        assertEquals (s, true, propertyString.isLeadIn ('$'));

        s = null;
        propertyString = new PropertyString(s);
        assertEquals (s, false, propertyString.isLeadIn ('$'));

        s = "";
        propertyString = new PropertyString(s);
        assertEquals (s, false, propertyString.isLeadIn ('$'));

        s = "$";
        propertyString = new PropertyString(s);
        assertEquals (s, false, propertyString.isLeadIn ('$'));
    }


    /**
     * Test of isCommand method, of class
     * de.iftm.dcm4che.router.property.PropertyString.
     */
    public void testIsCommand () {
        PropertyString propertyString = null;
        String         s;

        System.out.println ("testIsCommand");

        s = "#test";
        propertyString = new PropertyString(s);
        assertEquals (s, true, propertyString.isCommand ());

        s = "##test";
        propertyString = new PropertyString(s);
        assertEquals (s, false, propertyString.isCommand ());

        s = null;
        propertyString = new PropertyString(s);
        assertEquals (s, false, propertyString.isCommand ());

        s = "";
        propertyString = new PropertyString(s);
        assertEquals (s, false, propertyString.isCommand ());

        s = "#";
        propertyString = new PropertyString(s);
        assertEquals (s, false, propertyString.isCommand ());
    }


    /**
     * Test of getCommand method, of class
     * de.iftm.dcm4che.router.property.PropertyString.
     */
    public void testGetCommand () {
        PropertyString propertyString = null;
        String         s;

        System.out.println ("testGetCommand");

        s = "#test";
        propertyString = new PropertyString(s);
        assertEquals (s, "test", propertyString.getCommand ());

        s = "#";
        propertyString = new PropertyString(s);
        assertEquals (s, "", propertyString.getCommand ());

        s = null;
        propertyString = new PropertyString(s);
        assertEquals (s, "", propertyString.getCommand ());
    }


    /**
     * Test of getContents method, of class
     * de.iftm.dcm4che.router.property.PropertyString.
     */
    public void testGetContents () {
        Dataset    ds;
        PersonName pn;
        PropertyString propertyString = null;
        String         s;

        System.out.println ("testGetContents");

        // Leeres Dataset erzeugen
        ds = DcmObjectFactory.getInstance ().newDataset ();

        // Character Set für Westeuropa ISO-8859-1 festlegen
        // Anderenfalls wuerden deutsche Umlaute zu einer IllegalArgumentException
        // beim Befuellen des Dataset fuehren
        ds.putCS (Tags.SpecificCharacterSet, "ISO_IR 100");

        // Patient's Name: Group 0x0010, element 0x0010
        ds.putPN (Tags.PatientName, "Mustermann^Hans");

        // Patient ID: Group 0x0010, element 0x0020
        ds.putLO (Tags.PatientID, "12345");

        s = "hallo";
        propertyString = new PropertyString(s);
        assertEquals (s, "hallo", propertyString.getContents (ds));

        s = "#command";
        propertyString = new PropertyString(s);
        assertEquals (s, "", propertyString.getContents (ds));

        s = "$PatientName";
        propertyString = new PropertyString(s);
        assertEquals (s, "Mustermann^Hans", propertyString.getContents (ds));

        // PatientBirthDate ist im Dataset nicht definiert
        s = "$PatientBirthDate";
        propertyString = new PropertyString(s);
        assertEquals (s, "", propertyString.getContents (ds));

        // xxxxx ist kein Tag
        // s = "$xxxxx";
        // propertyString = new PropertyString(s);
        // assertEquals (s, "", propertyString.getContents (ds));

        s = "@00100010";
        propertyString = new PropertyString(s);
        assertEquals (s, "Mustermann^Hans", propertyString.getContents (ds));

        s = "@00100020";
        propertyString = new PropertyString(s);
        assertEquals (s, "12345", propertyString.getContents (ds));

        // Group 0x0001, element 0x0002 ist im Dataset nicht definiert
        s = "@00010002";
        propertyString = new PropertyString(s);
        assertEquals (s, "", propertyString.getContents (ds));

        // Group 0xyyyy, element 0xzzzz ist keine Hexadezimalzahl
        // s = "@yyyyzzzz";
        // propertyString = new PropertyString(s);
        // assertEquals (s, "", propertyString.getContents (ds));
    }
}
