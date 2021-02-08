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
import org.dcm4che.media.*;

import java.io.*;
import java.util.*;


/**
 * @author  hacklaender
 * @version 2006.07.01
 */
public class DicomDirReceiver extends GeneralFileReceiver {
    /*
     * Gives an oppertunity to recieve outprints at different appenders in
     * a better and faster way as use System.out
     */
    
    /** The version string */
    public final static String VERSION = "2006.11.10";
    
    /** DOCUMENT ME! */
    public static final Logger log = Logger.getLogger(DicomDirReceiver.class);
    
    /** The given DICOMDIR file. */
    private File dicomdir = null;
    
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>  Local fields defined by properties: >>>>>>>
    
    /** Holds the value of property fileURI. */
    private String file_uri = null;
    
    
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    
    /**
     * Creates a new instance of Class
     * @param props the properties for the receiver and plugins.
     */
    public DicomDirReceiver(Properties props) {
        super(props);
        
        // Initialise
        init();
    }
    
    
    /**
     * Creates a new instance of Class
     * @param uri the URI of the properties file for the receiver and plugins.
     */
    public DicomDirReceiver(String uri) throws IOException {
        super(uri);
        
        // Initialise
        init();
    }
    
    
    /**
     * Creates a new instance of Class
     * @param argv the arguments passed to the programs main method.
     */
    public DicomDirReceiver(String[] argv) {
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
        
        // First element in fileVector contains the File of the DICOMDIR.
        // Replace content of fileVector with the files referenced by DICOMDIR
        if (!replaceFileVectorWithDicomDir()) {
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
    
    
    /**
     * Replaces the content of fileVector with the files referenced by DICOMDIR.
     *
     * @return true, if a DICOMDIR was successful analysed.
     */
    private boolean replaceFileVectorWithDicomDir() {
        DirReader dirReader = null;
        DirRecord dr = null;
        Dataset ds;
        
        // If Vector equals to null, nothing to do
        if (fileVector == null) {
            log.error("No DICOMDIR selected");
            return false;
        }
        
        // First element in fileVector contains the File of the DICOMDIR.
        dicomdir = (File) fileVector.get(0);
        
        // Clear fileVector
        fileVector = new Vector();
        
        // The content of the directory records of a DICOMDIR are defined
        // in PS 3.3 - F.5 DEFINITION OF SPECIFIC DIRECTORY RECORDS.
        //
        // List of requieren Type 1 and 2 items:
        //
        // F.5.1 Patient Directory Record Definition
        //   - Specific Character Set, Type 1C
        //   - Patient's Name, Type 2
        //   - Patient ID, Type 1
        //
        // F.5.2 Study Directory record definition
        //   - Specific Character Set, Type 1C
        //   - Study Date, Type 1
        //   - Study Time, Type 1
        //   - Study Description, Type 2
        //   - Study Instance UID, Type 1C: The Study Instance UID shall be present as a mandatory key only if no file is referenced by this Directory Record.
        //   - Study ID, Type 1
        //   - Accession Number, Type 2
        //
        // F.5.3 Series Directory Record Definition
        //   - Specific Character Set, Type 1C
        //   - Modality, Type 1
        //   - Series Instance UID, Type 1
        //   - Series Number, Type 1
        //
        // F.5.4 Image directory record definition
        //   - Specific Character Set, Type 1C
        //   - Instance Number, Type 1
        
        try {
            // Get a DirReader
            dirReader = DirBuilderFactory.getInstance().newDirReader(dicomdir);
            
            // Get first Patient
            dr = dirReader.getFirstRecordBy(DirRecord.PATIENT, null, false);
        } catch (IOException e) {
            log.error("Selected file is not a DICOMDIR: " + e.getMessage());
            return false;
        }
        
        // Loop for all patients
        while (dr != null) {
            
            try {
                getNextChild(dr);
                
                // Get next Patient
                dr = dr.getNextSibling();
            } catch (IOException e) {
                log.error("Error while analysing the DICOMDIR: " + e.getMessage());
                return false;
            }
        }
        
        // DICOMDIR successful analysed
        return true;
    }
    
    
    /**
     *
     */
    private void getNextChild(DirRecord dr) throws IOException {
        DirRecord child;
        File f;
        
        child = dr.getFirstChild();
        
        // Loop for all childs
        while (child != null) {
            
            // Hanging Protocol DR and HL7 Struc Doc DR
            // are defined in PS 3.3 - F.5. but not in dcm4che 1.3.x
            // Should be catched before processing other types
            
            if (child.getType().equals(DirRecord.PATIENT)) {
                
                // PATIENT
                getNextChild(child);
                
            } else if (child.getType().equals(DirRecord.STUDY)) {
                
                // STUDY
                getNextChild(child);
                
            } else if (child.getType().equals(DirRecord.SERIES)) {
                // SERIES
                getNextChild(child);
                
            } else {
                
                // Allow all type of instance
                
                String[] sa = child.getRefFileIDs();
                
                // File ID must be defined
                if (!(sa == null)) {
                    
                    // Insert referenced file to fileVector
                    
                    // Start with DICOMDIR directory
                    f = dicomdir.getParentFile();
                    for (int i = 0; i < sa.length; i++) {
                        // Add descending IDs
                        f = new File(f, sa[i]);
                    }
                    
                    // log.info(f.toString());
                    
                    fileVector.addElement(f);
                }
            }
            
            child = child.getNextSibling();
        }
    }
    
}

/**
 * Revisions:
 *
 * 2006.04.29 hacklaender: Totally rewritten.
 */
