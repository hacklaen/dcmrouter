/*
 *  DicomReceiver.java
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
package de.iftm.dcm4che.router.receiver;

import de.iftm.dcm4che.router.property.*;
import de.iftm.dcm4che.router.plugin.DicomRouterPlugin;
import de.iftm.dcm4che.router.plugin.PluginChain;
import de.iftm.dcm4che.router.util.*;

import gnu.getopt.Getopt;

import org.dcm4che.data.*;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.security.GeneralSecurityException;

import org.apache.log4j.*;


/**
 * Abstract definition of all DicomReceiver's.
 * <p> Implementations of this class should send the following DicomRouterEvent's 
 * to registered listeners:
 * <p>STARTING_EVENT This event should be send before processing the first DICOM object.
 * <p>NEXT_OBJECT_EVENT This event should be send for each DICOM object before processing the object in the plugin chain.
 * <p>PROGRESS_EVENT If the total number of objects to process is known, this event should be send for each DICOM object before processing the object in the plugin chain.
 * <p>FINISHED_EVENT This event should be send after processing the last DICOM object.
 *
 * @author  hacklaender
 * @version 2006.11.10
 */
public abstract class DicomReceiver {
    
    /** DOCUMENT ME! */
    public static final Logger log = Logger.getLogger(DicomReceiver.class);
    
    
    /** Configuration properties. */
    protected DicomRouterProperties drProperties = null;
    
    /** Logger properties. */
    protected Properties loggerProperties = null;
    
    /** The collection of objects listening to DicomRouterEvents */
    protected Vector dicomRouterEventListeners = new Vector();
    
    
    /**
     * Constructor of a DicomReceiver.
     * <p>The properties must specify at least one plugin. The definition
     * <p>key = plugin.1.name, value = de.iftm.dcm4che.router.plugin.NullPlugin
     * <p>is sufficient.
     *
     * @param props the properties for the receiver and plugins.
     */
    public DicomReceiver(Properties props) {
        // Setup the properties
        setDicomRouterProperties(props);
    }
    
    
    /**
     * Constructor of a DicomReceiver.
     * <p>The referenced property file must specify at least one plugin. The definition
     * <p>plugin.1.name = de.iftm.dcm4che.router.plugin.NullPlugin
     * <p>is sufficient.
     *
     * @param uri the URI of the properties file for the receiver and plugins.
     */
    public DicomReceiver(String uri) throws IOException {
        // Setup the properties
        setDicomRouterProperties(uri);
    }
    
    
    /**
     * Constructor of a DicomReceiver.
     * <p>The referenced property file must specify at least one plugin. The definition
     * <p>plugin.1.name = de.iftm.dcm4che.router.plugin.NullPlugin
     * <p>is sufficient.
     *
     * @param argv the arguments passed to the programs main method.
     */
    public DicomReceiver(String[] argv) {
        Getopt  g;
        int     option;
        String  arg;
        String  propertiesURIString = null;
        String  loggerPropertiesURIString = null;
        
        // If no arguments are given
        if (argv == null) {
            showHelp();
            return;
        }
        
        // h showHelp, no arguments
        // l properties file for logger, argument required
        // p properties file for DicomRouter, argument required
        g = new Getopt("DicomReceiver", argv, "hl:p:");
        
        // geopt should not write error messages
        g.setOpterr(false);
        
        while ((option = g.getopt()) != -1) {
            switch (option) {
                case 'h':
                    showHelp();
                    break;
                    
                case'l':
                    loggerPropertiesURIString = g.getOptarg();
                    break;
                    
                case 'p':
                    propertiesURIString = g.getOptarg();
                    break;
                    
                case '?':
                    // Missing argument for an option
                    System.err.println("Missing argument for option.");
                    exit(null);
                    break;
                    
                default:
                    // Undefined option
                    System.err.println("Undefined option: " + (char) option);
                    exit(null);
                    ;
            }
        }
        
        if (propertiesURIString != null) {
            try {
                setDicomRouterProperties(propertiesURIString);
            } catch (IOException e) {
                // URI of DicomRouter properties could not accessed.
                System.err.println("Can't access URI of DicomRouter properties file.");
                exit(null);
            }
        } else {
            // DicomRouter properties must be defined.
            System.err.println("DicomRouter properties file not defined.");
            exit(null);
        }
        
        // If logger properties are not defined the BasicConfigurator is used
        try {
            setLoggerProperties(loggerPropertiesURIString);
        } catch (IOException e) {
            // URI of DicomRouter properties could not accessed.
            System.err.println("Can't access URI of logger properties file.");
            exit(null);
        }
    }
    
    
    //>>>> Abstract methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    
    /**
     *
     */
    public abstract boolean start();
    
    
    /**
     *
     */
    public abstract void stop();
    
    
    //>>>> Public methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    
    /**
     *
     * @param listener
     */
    public void addDicomRouterEventListener(DicomRouterEventListener listener) {
        // add a listener if it is not already registered
        if (!dicomRouterEventListeners.contains(listener)) {
            dicomRouterEventListeners.addElement(listener);
        }
    };
    
    
    /**
     *
     * @param listener
     */
    public void removeDicomRouterEventListener(DicomRouterEventListener listener) {
        // remove a listener if it is not already registered
        if (dicomRouterEventListeners.contains(listener)) {
            dicomRouterEventListeners.removeElement(listener);
        }
    };
    
    
    /**
     * Notify all listener to DicomRouterEvent.
     * @param evt the event object.
     */
    protected void fireDicomRouterEvent(DicomRouterEvent evt) {
        Vector v = null;
        
        if (dicomRouterEventListeners == null) return;
        
        // make a copyof the listener object so that it cannot be
        // changed while we are firing events
        synchronized(this) {
            v = (Vector) dicomRouterEventListeners.clone();
        }
        
        // fire the event
        for (int i = 0; i < v.size(); i++) {
            DicomRouterEventListener client = (DicomRouterEventListener) v.elementAt(i);
            client.handleDicomRouterEvent(evt);
        }
    }
    
    
    /**
     *
     * @param props
     */
    public void setDicomRouterProperties(Properties props) {
        this.drProperties = new DicomRouterProperties(props);
    }
    
    
    /**
     *
     * @param uri
     */
    public void setDicomRouterProperties(String uri) throws IOException {
        setDicomRouterProperties(new DicomRouterProperties(uri));
    }
    
    
    /**
     *
     * @param props
     */
    public void setLoggerProperties(Properties props) {
        this.loggerProperties = props;
        
        // Reset the default hierarchy to its defaut.
        BasicConfigurator.resetConfiguration();
        // Set log level to INFO
        Logger.getRootLogger().setLevel(Level.INFO);
        
        if (loggerProperties != null) {
            
            // Read configuration options from properties.
            PropertyConfigurator.configure(loggerProperties);
            
        } else {
            
            // Add a ConsoleAppender that uses PatternLayout using the
            // PatternLayout.TTCC_CONVERSION_PATTERN and prints to System.out
            // to the root category.
            BasicConfigurator.configure();
            
            // Set logging level to INFO
            Logger.getRootLogger().setLevel(Level.INFO);
            
            // Set logging level of dcm4che to WARN
            Logger.getLogger("org.dcm4cheri.net.FsmImpl").setLevel(Level.WARN);
            Logger.getLogger("org.dcm4cheri.server.ServerImpl").setLevel(Level.WARN);
            
        }
    }
    
    /**
     *
     * @param uri
     */
    public void setLoggerProperties(String uri) throws IOException {
        Properties props = null;
        
        props.load(new FileInputStream(Util.uriToFile(uri)));
        
        setLoggerProperties(props);
    }
    
    
    /**
     * Displays the showHelp message on the System.out device.
     */
    private static void showHelp() {
        System.out.println("Usage: DicomXXXReceiver [-h] [-l filename] [-p filename]");
        System.out.println("-h print this help message.");
        System.out.println("-l set the filename of the properties file for the logger.");
        System.out.println("-p set the filename of the properties file for the DicomRouter.");
    }
    
    
    /**
     * Displays a showCopyright notice on the System.out device.
     */
    protected static void showCopyright() {
        System.out.println(Util.getCopyrightMessage());
    }
    
    
    /**
     * The application is closed if called
     * @param prompt Errormessage
     */
    public void exit(String prompt) {
        if (prompt != null) {
            System.err.println(prompt);
            log.error(prompt);
        }
        
        System.exit(1);
    }
    
}

/**
 * Revisions:
 *
 * 2006.04.29, hacklaender: Totally rewritten.
 * 2005.08.22, hacklaender: method execute renamed to start (for consistance with a future implementation of a stop method).
 * 2005.08.21, hacklaender: initProperties method added.
 */
