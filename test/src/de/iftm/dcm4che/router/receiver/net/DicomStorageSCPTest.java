/*
 *  DicomStorageSCPTest.java
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
import de.iftm.dcm4che.router.receiver.DicomReceiver;
import de.iftm.dcm4che.router.server.ServerExt;
import de.iftm.dcm4che.router.server.ServerFactoryExt;
import de.iftm.dcm4che.router.util.CommandLineOpt;
import de.iftm.dcm4che.router.property.*;
import org.apache.log4j.*;
import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.net.*;
import org.dcm4che.server.*;
import org.dcm4che.util.*;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.nio.*;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;


/**
 * JUnit based test for the class: DicomStorageSCP.java
 *
 * @author Thomas Hacklaender
 * @version 2005.08.29
 */
public class DicomStorageSCPTest extends TestCase {

    
    public DicomStorageSCPTest(String testName) {
        super(testName);
    }

    
    protected void setUp() throws Exception {
        // Alle bestehenden Appender loeschen und Logging Level auf INFO setzen
        BasicConfigurator.resetConfiguration();
        
        // Logging System initialisieren
        BasicConfigurator.configure();
        
        // Logging Level auf INFO setzen
        Logger.getRootLogger().setLevel(Level.INFO);
    }

    
    protected void tearDown() throws Exception {
    }

    
    public static Test suite() {
        TestSuite suite = new TestSuite(DicomStorageSCPTest.class);
        
        return suite;
    }

    
    /**
     * Test of init method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageSCP.
     */
    public void testInit() {
        System.out.println("testInit");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of loadConfiguration method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageSCP.
     */
    public void testLoadConfiguration() {
        System.out.println("testLoadConfiguration");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of extractPresentationContextFromConfiguration method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageSCP.
     */
    public void testExtractPresentationContextFromConfiguration() {
        System.out.println("testExtractPresentationContextFromConfiguration");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of start method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageSCP.
     */
    public void testStart() {
        System.out.println("testStart");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of getStorageSCPReceiverAssociationHandler method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageSCP.
     */
    public void testGetStorageSCPReceiverAssociationHandler() {
        System.out.println("testGetStorageSCPReceiverAssociationHandler");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of setStorageSCPReceiverAssociationHandler method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageSCP.
     */
    public void testSetStorageSCPReceiverAssociationHandler() {
        System.out.println("testSetStorageSCPReceiverAssociationHandler");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of c_store method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageSCP.
     */
    public void testC_store() {
        System.out.println("testC_store");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }

    /**
     * Test of exit method, of class de.iftm.dcm4che.router.receiver.net.DicomStorageSCP.
     */
    public void testExit() {
        System.out.println("testExit");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }
    
}
