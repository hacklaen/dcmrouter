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

import de.iftm.dcm4che.router.receiver.*;
import de.iftm.dcm4che.router.plugin.DicomRouterPlugin;
import de.iftm.dcm4che.router.plugin.PluginChain;
import de.iftm.dcm4che.router.property.*;
import de.iftm.dcm4che.router.util.*;

import org.dcm4che.data.*;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import org.apache.log4j.Logger;


/**
 * @author  hacklaender
 * @version 2006.11.10
 */
public abstract class GeneralFileReceiver  extends DicomReceiver {
    /*
     * Gives an oppertunity to recieve outprints at different appenders in
     * a better and faster way as use System.out
     */
    
    /** The version string */
    public final static String VERSION = "2006.11.10";
    
    /** DOCUMENT ME! */
    public static final Logger log = Logger.getLogger(DicomFileReceiver.class);
    
    /** Holds the properties of the FileReceiver. */
    protected Properties props = null;
    
    /** Holds the Files to process. */
    protected Vector fileVector = null;
    
    /** Holds the chain of plugins. */
    protected PluginChain pluginChain = null;
    
    /** Signals the thread to stop */
    protected boolean stopFlag = false;

    
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
    public GeneralFileReceiver(Properties props) {
        super(props);
    }
    
    
    /**
     * Creates a new instance of Class
     * @param uri the URI of the properties file for the receiver and plugins.
     */
    public GeneralFileReceiver(String uri) throws IOException {
        super(uri);
    }
    
    
    /**
     * Creates a new instance of Class
     * @param argv the arguments passed to the programs main method.
     */
    public GeneralFileReceiver(String[] argv) {
        super(argv);
    }
    
    
    //>>>> Static methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
      
    /**
     *
     */
    static public GeneralFileReceiver createByName(File propFile) {
        Class                   theClass;
        Constructor             theClassConstructor;
        DicomRouterProperties   drProps;
        GeneralFileReceiver     fileReceiver;
        Properties              receiverProps;
        String                  receiverName;
        boolean                 success;
        Class[]                 parameterTypes = new Class[] {Properties.class};
        Object[]                initargs;
        
        try {
            drProps = new DicomRouterProperties(propFile);
        } catch (IOException e) {
            log.error("Can't load properties for receiver");
            return null;
        }
        receiverProps = drProps.extractReceiverProperties();
        receiverName = receiverProps.getProperty("name");
        if (receiverName == null) {
            log.error("No name for a file receiver given in properties");
            return null;
        }
        
        try {
            theClass = Class.forName(receiverName);
        } catch (Exception e) {
            log.error("Can't load receiver: " + receiverName);
            return null;
        }
        
        try {
            theClassConstructor = theClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            log.error("Can't get constructor of receiver: " + receiverName);
            return null;
        }
        
        initargs = new Object[] {drProps};
        try {
            fileReceiver = (GeneralFileReceiver) theClassConstructor.newInstance(initargs);
        } catch (Exception e) {
            log.error("Can't instantiate receiver: " + receiverName);
            return null;
        }
        
        return fileReceiver;
    }
    
    
    //>>>> Other methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
      
    /**
     * Copies the relevant properties to local fields.
     */
    protected void init() {
        String  s;
        File f;
        
        // Use properties set in the parent class DicomReceiver
        // Store the properties in a local field
        props = drProperties.extractReceiverProperties();
        
        if (props == null) {
            // nichts zu tun
            return;
        }
        
        // file_uri
        s = props.getProperty("file-uri");
        if (s != null) {
            // URI speichern
            file_uri = s;
        }
        
        // include_subdirectories
        s = props.getProperty("include-subdirectories");
        if (s != null) {
            if (s.toLowerCase().charAt(0) == 't') {
                include_subdirectories = true;
            } else {
                include_subdirectories = false;
            }
        }
        
        // Vector von Files festlegen
        f = Util.uriToFile(file_uri);
        setFileToProcess(f);
        
    }
    
    /**
     * Should be called after setDicomRouterProperties to consider the router properties.
     *
     * @param f
     */
    public void setFileToProcess(File f) {
        setFileVectorToProcess(fileToFileVector(f));
    }
    
    
    /**
     * Should be called after setDicomRouterProperties to consider the router properties.
     *
     * @param v
     */
    public void setFileVectorToProcess(Vector v) {
        fileVector = v;
    }

 
      
    /**
     * Generates a Vector of files from the local fields file_uri and include_subdirectories.
     *
     * @param f The file to process.
     * @return The Vector or null in the case of an error.
     */
    protected Vector fileToFileVector(File f) {
        File[] fileList = null;
        Vector fVec = new Vector();
        
        if (f == null) {
            //nichts zu tun
            return null;
        }
        
        // Wenn der Pfad ungueltig ist null zurueckgeben
        if (!f.exists()) {
            return null;
        }
        
        // File(s) zum Filevector hinzufuegen
        addFile(f, fVec);
        
        return fVec;
    }
    
    
    /**
     * Adds one or more files to a given Vector of File's.
     *
     * @param f The file or directory to add.
     * @param fVec The Vector of files.
     */
    protected void addFile(File f, Vector fVec) {
        File[] fList;
        
        if (f.isFile()) {
            // f ist ein File: nur diesen File zum Vector hinzufuegen
            fVec.add(f);
            
            return;
        } else {
            // f ist ein Directory: Alle Files aus dem Diectory hinzufuegen
            // Liste aller Files und Subdirectories erstellen
            fList = f.listFiles();
            
            // Liste abarbeiten
            for (int i = 0; i < fList.length; i++) {
                if (include_subdirectories) {
                    // Subdirectories mit einschliessen: Recursiver Aufruf
                    addFile(fList[i], fVec);
                } else {
                    // Subdirectories nicht einschliessen: Ignorieren
                    if (fList[i].isFile()) {
                        fVec.add(fList[i]);
                    }
                }
            }
        }
    }
    
    
    //>>>> Thread to process file vector >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    
    /**
     * Process the files in a separed Thread.
     * <p>This implementation assumes, that the read in file contains already a 
     * valid DICOM object.
     */
    class ProcessFileVector extends Thread {
        
        public void run() {
            File                f = null;
            BufferedInputStream in = null;
            FileFormat          ff = null;
            Dataset             ds = null;
            int                 chainResultCode;
            DicomRouterEvent    dre;
                       
            // Notify listeners: STARTING_EVENT
            dre = new DicomRouterEvent(this);
            dre.setStartingEvent();
            fireDicomRouterEvent(dre);
            
            process_filevector_block: for (int i = 0; i < fileVector.size(); i++) {
                
                if (stopFlag) {
                    // Abort processing the file vector
                    break process_filevector_block;
                }
                
                try {
                    f = (File) fileVector.get(i);
                    in = new BufferedInputStream(new FileInputStream(f));
                } catch (IOException ioe) {
                    log.error("Can't read file: " + f.getPath());
                    // Try next file
                    continue;
                }
                
                DcmParser  parser = DcmParserFactory.getInstance().newDcmParser(in);
                
                try {
                    ff = parser.detectFileFormat();
                } catch (Exception ioe) {
                    log.error("Can't detect DICOM file format for file: " + f.getPath());
                    // Try next file
                    continue;
                }
                
                ds = DcmObjectFactory.getInstance().newDataset();
                
                try {
                    ds.readFile(in, ff, -1);
                } catch (IOException ioe) {
                    log.error("Can't create Dataset for file: " + f.getPath());
                    // Try next file
                    continue;
                }
                
                // Notify listeners: PROGRESS_EVENT
                dre = new DicomRouterEvent(this);
                dre.setProgressEvent(i, fileVector.size()- 1);
                fireDicomRouterEvent(dre);
                
                // Notify listeners: NEXT_OBJECT_EVENT
                dre = new DicomRouterEvent(this);
                dre.setNextObjectEvent(ds);
                fireDicomRouterEvent(dre);
                
                // Process Dataset
                chainResultCode = pluginChain.process(ds);
                
                if (chainResultCode != DicomRouterPlugin.CONTINUE) {
                    // Abort processingthe DICOM object set
                    break process_filevector_block;
                }
            }
                       
            // Notify listeners: FINISHED_EVENT
            dre = new DicomRouterEvent(this);
            dre.setFinishedEvent();
            fireDicomRouterEvent(dre);
            
            // Close all plugins
            pluginChain.closePlugins();
        }
    }
    
}

/**
 * Revisions:
 *
 * 2006.04.29 hacklaender: Totally rewritten.
 */
