/*
 *  DicomReceiverTest.java
 *
 *  Copyright (c) 2005 by
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

package de.iftm.dcm4che.router.receiver;

import junit.framework.*;
import org.apache.log4j.*;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.security.GeneralSecurityException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import de.iftm.dcm4che.router.property.*;

/**
 * JUnit based test for the class: DicomReceiver.java
 *
 * @author Thomas Hacklaender
 * @version 2005.08.29
 */
public class DicomReceiverTest extends TestCase {
    
    /** The class to test */
    DicomReceiverImpl  dicomReceiverImpl = null;

    
    public DicomReceiverTest(String testName) {
        super(testName);
    }

    
    protected void setUp() throws Exception {
        // Alle bestehenden Appender loeschen und Logging Level auf INFO setzen
        BasicConfigurator.resetConfiguration();
        
        // Logging System initialisieren
        BasicConfigurator.configure();
        
        // Logging Level auf INFO setzen
        Logger.getRootLogger().setLevel(Level.INFO);
        
        // Instanz von DicomStorageSCU erzeugen und mit leeren Properties initialisieren
        dicomReceiverImpl = new DicomReceiverImpl();
    }

    
    protected void tearDown() throws Exception {
    }

    
    public static Test suite() {
        TestSuite suite = new TestSuite(DicomReceiverTest.class);
        
        return suite;
    }

    
    /**
     * Test of setRouterProperties method, of class de.iftm.dcm4che.router.receiver.DicomReceiver.
     */
    public void testSetRouterProperties() {
        System.out.println("testSetRouterProperties");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of setLoggerProperties method, of class de.iftm.dcm4che.router.receiver.DicomReceiver.
     */
    public void testSetLoggerProperties() {
        System.out.println("testSetLoggerProperties");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of initDicomReceiverProperties method, of class de.iftm.dcm4che.router.receiver.DicomReceiver.
     */
    public void testInitDicomReceiverProperties() {
        System.out.println("testInitDicomReceiverProperties");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of start method, of class de.iftm.dcm4che.router.receiver.DicomReceiver.
     */
    public void testStart() {
        System.out.println("testStart");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of uriToProperties method, of class de.iftm.dcm4che.router.receiver.DicomReceiver.
     */
    public void testUriToProperties() {
        System.out.println("testUriToProperties");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of copyright method, of class de.iftm.dcm4che.router.receiver.DicomReceiver.
     */
    public void testCopyright() {
        System.out.println("testCopyright");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of exit method, of class de.iftm.dcm4che.router.receiver.DicomReceiver.
     */
    public void testExit() {
        System.out.println("testExit");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    
    /**
     * Generated implementation of abstract class de.iftm.dcm4che.router.receiver.DicomReceiver. Please fill dummy bodies of generated methods.
     */
    private class DicomReceiverImpl extends DicomReceiver {

        protected void init(java.util.Properties props) {
            // TODO fill the body in order to provide useful implementation
            
        }

        public void start() {
            // TODO fill the body in order to provide useful implementation
            
        }
    }

    
}
