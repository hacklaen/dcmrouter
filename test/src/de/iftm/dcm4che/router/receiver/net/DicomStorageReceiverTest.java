/*
 *  DicomStorageReceiverTest.java
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

package de.iftm.dcm4che.router.receiver.net;

import junit.framework.*;
import org.apache.log4j.*;

import de.iftm.dcm4che.router.plugin.PluginChain;
import de.iftm.dcm4che.router.plugin.PluginChain;
import de.iftm.dcm4che.router.property.*;
import de.iftm.dcm4che.router.receiver.*;
import de.iftm.dcm4che.router.util.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dcm4che.util.*;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.util.*;
import java.io.*;
import java.security.GeneralSecurityException;

/**
 * JUnit based test for the class: DicomStorageReceiver.java
 *
 * @author Thomas Hacklaender
 * @version 2005.08.29
 */
public class DicomStorageReceiverTest extends TestCase {
    
    /** The class to test */
    DicomStorageReceiver  dicomStorageReceiver = null;

    
    public DicomStorageReceiverTest(String testName) {
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
        dicomStorageReceiver = new DicomStorageReceiver();
    }

    
    protected void tearDown() throws Exception {
    }

    
    public static Test suite() {
        TestSuite suite = new TestSuite(DicomStorageReceiverTest.class);
        
        return suite;
    }

    /**
     * Test of initDicomReceiverProperties method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageReceiver.
     */
    public void testInitDicomReceiverProperties() {
        System.out.println("testInitDicomReceiverProperties");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    
    /**
     * Test of start method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageReceiver.
     */
    public void testStart() {
        System.out.println("testStart");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of main method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageReceiver.
     */
    public void testMain() {
        System.out.println("testMain");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of tokenize method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageReceiver.
     */
    public void testTokenize() {
        System.out.println("testTokenize");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }
    
}
