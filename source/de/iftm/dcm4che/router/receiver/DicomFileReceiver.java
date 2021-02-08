/*
 *  Copyright (c) 2006 by
 *
 *  Thomas Hacklaender @ IFTM Institut für Telematik in der Medizn GmbH
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
 *****************************************************************************/
package de.iftm.dcm4che.router.receiver;

import de.iftm.dcm4che.router.plugin.DicomRouterPlugin;
import de.iftm.dcm4che.router.plugin.PluginChain;
import de.iftm.dcm4che.router.property.*;
import de.iftm.dcm4che.router.util.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.dcm4che.data.*;
import org.dcm4che.dict.Tags;

import java.io.*;
import java.util.*;


/**
 * @author  kleber, hacklaender
 * @version 2006.04.24
 */
public class DicomFileReceiver extends GeneralFileReceiver {
    /*
     * Gives an oppertunity to recieve outprints at different appenders in
     * a better and faster way as use System.out
     */
    
    /** The version string */
    public final static String VERSION = "2006.11.10";
    
    /** DOCUMENT ME! */
    public static final Logger log = Logger.getLogger(DicomFileReceiver.class);

    
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>  Local fields defined by properties: >>>>>>>
    
    /** Holds the value of property fileURI. */
    private String file_uri = null;
    
    /** Holds the value of property include_subdirectories. */
    private boolean include_subdirectories = false;

    
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
       
    /**
     * Creates a new instance of Class
     * @param props the properties for the receiver and plugins.
     */
    public DicomFileReceiver(Properties props) {
        super(props);
         
        // Initialise
        init();
    }
    
    
    /**
     * Creates a new instance of Class
     * @param uri the URI of the properties file for the receiver and plugins.
     */
    public DicomFileReceiver(String uri) throws IOException {
        super(uri);
         
        // Initialise
        init();
    }
    
    
    /**
     * Creates a new instance of Class
     * @param argv the arguments passed to the programs main method.
     */
    public DicomFileReceiver(String[] argv) {
        super(argv);
        
        // Initialise
        init();
        
        // Start automatically
        start();
    }
    
       
    //>>>> Static methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
 
    
    /**
     * @param argv the command line arguments.
     */
    public static void main(String[] argv) {
        new DicomFileReceiver(argv);
    }
    
    
    //>>>> Other methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    
    
    /**
     * Extend the init method of GeneralFileReceiver
     */
    protected void init() {

        // First use GeneralFileReceiver.init()
        super.init();
        
        // No local extensions
    }
     
    
    /**
     * @return true, if successful started, false otherwise.
     */
    public boolean start() {
        
        // Wenn der Vector null ist, ist nichts zu tun
        if (fileVector == null) {
            return false;
        }
        
        // Info
        if (log.isInfoEnabled()) {
            log.info("Version: " + this.VERSION);
            log.info("Number of files to process: " + fileVector.size());
        }
        
        try {
            pluginChain = new PluginChain(drProperties);
        } catch (DicomRouterException e) {
            log.error(e.getMessage());
            return false;
        }
        
        pluginChain.initPlugins();
        
        // Bearbeitung in separaten Thread starten
        Thread pfv = new ProcessFileVector();
        pfv.start();
        
        return true;
    }
    
    
    /**
     * Send a "stop" signal to the process method
     */
    public void stop() {
        stopFlag = true;
    }
    
}

/**
 * Revisions:
 *
 * 2006.11.10 hacklaender: Use ProcessFileVector class in super class.
 * 2006.04.29 hacklaender: Totally rewritten.
 */
