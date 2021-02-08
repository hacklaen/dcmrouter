/*
 *  UnitTest: DicomStorageSCUTest.java
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
 */

package de.iftm.dcm4che.router.plugin.dicomsend;

import junit.framework.*;
import org.apache.log4j.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.net.*;
import org.dcm4che.server.*;
import org.dcm4che.util.*;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.net.ssl.*;

import de.iftm.dcm4che.router.property.*;

/**
 * JUnit based test for the class: SaveDicomdirPlugin.java
 * <br>
 * @author Thomas Hacklaender
 * @version 2005.08.20
 */
public class DicomStorageSCUTest extends TestCase {
    
    /** The class to test */
    DicomStorageSCU  dicomStorageSCU = null;
    
    
    public DicomStorageSCUTest(String testName) {
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
        dicomStorageSCU = new DicomStorageSCU();
    }
    
    
    protected void tearDown() throws Exception {
    }
    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DicomStorageSCUTest.class);
        
        return suite;
    }
    
    /**
     * Test of init method, of class de.iftm.dcm4che.router.plugin.dicomsend.DicomStorageSCU.
     */
    public void testInit() {
        System.out.println("testInit");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }
    
    /**
     * Test of loadConfiguration method, of class de.iftm.dcm4che.router.plugin.dicomsend.DicomStorageSCU.
     */
    public void testLoadConfiguration() {
        String value;
        
        System.out.println("testLoadConfiguration");
        
        // DicomStorageSCU muss mindestens mit einer DcmURL initialisiert sein
        dicomStorageSCU.url = new DcmURL("dicom", "DCMRCV", "DCMSND", "127.0.0.1", 105);
        
        // Einige Field gezielt auf falsche Werte setzen
        dicomStorageSCU.buf_len = -1;
        dicomStorageSCU.max_op_invoked = -1;
        dicomStorageSCU.max_pdu_len = -1;
        dicomStorageSCU.prior = -1;
        dicomStorageSCU.repeat_assoc = -1;
        dicomStorageSCU.tls_key = "aaa";
        dicomStorageSCU.tls_key_passwd = "bbb";
        dicomStorageSCU.tls_cacerts = "ccc";
        dicomStorageSCU.tls_cacerts_passwd = "ddd";
        
        // Configuration einlesen
        dicomStorageSCU.loadConfiguration();
        
        // Testen, ob Fields wieder korrigiert wurden
        assertEquals(dicomStorageSCU.buf_len, 2048);
        assertEquals(dicomStorageSCU.max_op_invoked, 0);
        assertEquals(dicomStorageSCU.max_pdu_len, 16352);
        assertEquals(dicomStorageSCU.prior, 0);
        assertEquals(dicomStorageSCU.repeat_assoc, 1);
        assertEquals(dicomStorageSCU.tls_key, "dcmsnd.jks");
        assertEquals(dicomStorageSCU.tls_key_passwd, "passwd");
        assertEquals(dicomStorageSCU.tls_cacerts, "cacerts.jks");
        assertEquals(dicomStorageSCU.tls_cacerts_passwd, "passwd");
        
        // Testet Transfersytaxes
        
        // ExplicitVRLittleEndian
        assertEquals(dicomStorageSCU.assocRQ.getPresContext(1).getTransferSyntaxUIDs().get(0), "1.2.840.10008.1.2.1");
        
        // ImplicitVRLittleEndian
        assertEquals(dicomStorageSCU.assocRQ.getPresContext(1).getTransferSyntaxUIDs().get(1), "1.2.840.10008.1.2");
        
        // JPEGLossless
        assertEquals(dicomStorageSCU.assocRQ.getPresContext(95).getTransferSyntaxUIDs().get(0), "1.2.840.10008.1.2.4.70");
    }
    
    /**
     * Test of openAssoc method, of class de.iftm.dcm4che.router.plugin.dicomsend.DicomStorageSCU.
     */
    public void testOpenAssoc() {
        System.out.println("testOpenAssoc");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }
    
    /**
     * Test of send method, of class de.iftm.dcm4che.router.plugin.dicomsend.DicomStorageSCU.
     */
    public void testSend() {
        System.out.println("testSend");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }
    
    /**
     * Test of abort method, of class de.iftm.dcm4che.router.plugin.dicomsend.DicomStorageSCU.
     */
    public void testAbort() {
        System.out.println("testAbort");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }
    
    /**
     * Test of closeSession method, of class de.iftm.dcm4che.router.plugin.dicomsend.DicomStorageSCU.
     */
    public void testCloseSession() {
        System.out.println("testCloseSession");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }
    
    /**
     * Test of sendDataset method, of class de.iftm.dcm4che.router.plugin.dicomsend.DicomStorageSCU.
     */
    public void testSendDataset() {
        System.out.println("testSendDataset");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }
    
    
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    
    
    public static class MyDataSourceForDcmSndTest extends TestCase {
        
        public MyDataSourceForDcmSndTest(java.lang.String testName) {
            
            super(testName);
        }
        
        protected void setUp() throws Exception {
        }
        
        protected void tearDown() throws Exception {
        }
        
        public static Test suite() {
            TestSuite suite = new TestSuite(MyDataSourceForDcmSndTest.class);
            
            return suite;
        }
        
        /**
         * Test of writeTo method, of class de.iftm.dcm4che.router.plugin.dicomsend.DicomStorageSCU.MyDataSourceForDcmSnd.
         */
        public void testWriteTo() {
            System.out.println("MyDataSourceForDcmSndTest.testWriteTo");
            
            // TODO add your test code below by replacing the default call to fail.
            // fail("The test case is empty.");
        }
        
        /**
         * Test of copy method, of class de.iftm.dcm4che.router.plugin.dicomsend.DicomStorageSCU.MyDataSourceForDcmSnd.
         */
        public void testCopy() {
            System.out.println("MyDataSourceForDcmSndTest.testCopy");
            
            // TODO add your test code below by replacing the default call to fail.
            // fail("The test case is empty.");
        }
    }
    
}
