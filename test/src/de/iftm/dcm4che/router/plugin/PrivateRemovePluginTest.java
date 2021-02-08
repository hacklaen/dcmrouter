/*
 * TestSuite SimpleTest.java
 *
 *  Copyright (c) 2003 by
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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;

import java.awt.*; // list of common used packages
import java.awt.event.*;

import java.io.*;
import java.io.ByteArrayInputStream;

import java.util.*;

import javax.swing.*;


/**
 *  JUnit TestSuite for SimpleTest
 *  <BR>
 *     Start main to run testcases or
 *      execute junit.swingui.TestRunner.run( SimpleTest.class )
 *      to execute visual test.
 *
 *
 * <DL>
 *        <DT><B>Revision:</B></DT>
 *        <DD>$Revision: 1.5 $</DD>
 *        <DT><B>Date:</B></DT>
 *        <DD>$Date: 2004/02/01 20:31:52 $</DD>
 * </DL>
 *
 * @author hacklaender
 * @version 2006.11.17
 */
public class PrivateRemovePluginTest
    extends TestCase {
    //////////////////////////////////////////
    //  F I X T U T R E  T E S T   S E T S
    //  defines the test-sets
    //  used by every TestCase
    //////////////////////////////////////////
    //  TestSet-Data
    //  TestObject my_test_object_a = null;
    //  TestObject my_test_object_b = null;
    public PrivateRemovePluginTest (java.lang.String testName) {
        super(testName);
    }

    /**
     *  Initializes the testset.
     *  This method is being executed before
     *  any TestCase (testMethod) is being called.
     */
    public void setUp () {
        // Alle bestehenden Appender loeschen und Logging Level auf INFO setzen
        BasicConfigurator.resetConfiguration ();

        // Logging System initialisieren
        BasicConfigurator.configure ();

        // Logging Level auf INFO setzen
        Logger.getRootLogger ().setLevel (Level.INFO);
    }


    /**
     *  Deinitializes the testset.
     *  This method is being executed before
     *  any TestCase (testMethod) is being called.
     */
    public void tearDown () {
        // my_test_object_a.dispose();
        // my_test_object_b.dispose();
    }


    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
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
        TestSuite suite = new TestSuite(PrivateRemovePluginTest.class);

        return suite;
    }


    /**
     * Test
     */
    public void testSingle ()
        throws Exception {
        System.out.println ("testSingle");

        Dataset ds = DcmObjectFactory.getInstance ().newDataset ();

        ds.putUN (0x00770077, new byte[10]);
        System.out.println ("##### - before ####");
        ds.dumpDataset (System.out, null);

        PrivateRemovePlugin privateRemovePlugin = new PrivateRemovePlugin();

        privateRemovePlugin.init (new Properties());
        privateRemovePlugin.process (ds);
        System.out.println ("##### - after - ####");
        this.assertEquals (ds.size (), 0);
    }


    /**
     * Test
     */
    public void testMuliple ()
        throws Exception {
        System.out.println ("testMuliple");

        Dataset ds = DcmObjectFactory.getInstance ().newDataset ();

        ds.putUN (0x00770077, new byte[10]);
        ds.putUT (0x0040a160, new String("das ist ein neuer Wert"));
        ds.putUN (0x00770055, new byte[10]);
        System.out.println ("##### - before ####");
        ds.dumpDataset (System.out, null);

        PrivateRemovePlugin privateRemovePlugin = new PrivateRemovePlugin();

        privateRemovePlugin.init (new Properties());
        privateRemovePlugin.process (ds);
        System.out.println ("##### - after - ####");
        ds.dumpDataset (System.out, null);
        this.assertEquals (ds.size (), 1);
        this.assertTrue (ds.contains (0x0040a160));
    }
}


/*
 * $Log: PrivateRemovePluginTest.java,v $
 * Revision 1.5  2004/02/01 20:31:52  hacklaen
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/13 18:54:53  k_kleber
 * init
 *
 * Revision 1.2  2002/11/29 17:31:40  stroeter
 * Jalopy Pretty Printer default settings applied
 * (Java Code Conventions)
 * ppjava -r
 *
 * Revision 1.1  2002/11/27 09:17:36  kleber
 * add replacer
 *
 * Revision 1.1  2002/11/26 10:37:59  kleber
 * add replacer
 *
 * Revision 1.5  2002/06/17 09:11:53  kleber
 * JiveX-3_3-branch-merged_1
 *
 * Revision 1.4.8.1  2002/06/17 07:44:46  kleber
 * no message
 *
 * Revision 1.4  2001/09/18 13:20:36  stroeter
 * removed fail
 *
 * Revision 1.3  2001/05/08 14:45:38  stroeter
 * added: method replaceString
 *
 * Revision 1.2  2001/04/26 18:18:27  stroeter
 * bugfix: StringTokenizer removed single
 *  backslash from String so this class and
 * the TestCase have been changed to fix/detect
 * it.
 *
 * Revision 1.1  2001/04/26 13:24:30  stroeter
 * added: method breakQuotedString() to
 *         split up a String
 *
 */
