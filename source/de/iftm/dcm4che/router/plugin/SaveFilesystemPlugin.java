/*
 *  SaveFilesystemPlugin.java
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

import de.iftm.dcm4che.router.property.*;

import de.iftm.dcm4che.router.util.*;

import org.apache.log4j.*;

import org.dcm4che.data.*;

import org.dcm4che.dict.*;

import java.io.*;

import java.net.*;

import java.util.*;


/**
 * This plugins saves the given Dataset to the lokal filesystem. It supports
 * the properties which start with the key SaveFilesystemPlugin. For description
 * of the subkeys (= properties) see the user manual.<br>
 *
 * @author Thomas Hacklaender
 * @version 2006.04.24
 */
public class SaveFilesystemPlugin extends DicomRouterPlugin {
    /** The version string */
    final String VERSION = "2006.11.10";
    
    /** The logger for this plugin */
    Logger log = Logger.getLogger(SaveFilesystemPlugin.class);
    
    /** The properties of this plugin */
    Properties props = null;
    
    
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>  Local fields defined by properties: >>>>>>>
    
    
    /** Directory to save the Dataset */
    File directory = Util.uriToFile("./");
    
    /** Extension of the file to save */
    String extension = "dcm";
    
    /**
     * Name of file to save. If the String starts with the character '$' the filename
     * is set from the DICOM element named in the remaining part of the string.
     */
    String filename = "DicomRouterDataset";
    
    /** If true, write files in the subdirectory "$PatientName_$PatientBirthDate/<directory>/" */
    boolean separate_patients = false;
    
    /** Transfersyntax of the saved file. One of the strings  ImplicitVRLittleEndian,
     * ExplicitVRLittleEndian, ExplicitVRBigEndian. The string may start with
     * the praefix-character '$' */
    String transfersyntax = "ImplicitVRLittleEndian";
    
    /** Write files in the subdirectory "[$PatientName_$PatientBirthDate/]<directory>/<use_subdirectory>/". */
    String use_subdirectory = "";
    
    /**
     * If true:
     * Construct the the file ID of the file to save from the following file ID
     * components:<br>
     * 1. First letter of family name followed by first letter of given name
     * followed by date of birth, 6 character. Example: HT570522<br>
     * 2. Study date, 6 character. Example: 043012<br>
     * 3. Modality, 2 character followed by study time, 4 character. Example: MR1531<br>
     * 4. Study ID. Example: 4711<br>
     * 5. Series number. Example: 3<br>
     * 6. Instance number. Example: 54<br>
     * If false:<br>
     * Construct the the name of the file to save from directory, filename and extension.
     */
    boolean write_dir_tree = true;
    
    /** Include a File Meta Information block to the saved file */
    boolean write_fmi = true;
    
    
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    
    
    /**
     * Constructor.
     */
    public SaveFilesystemPlugin() {
        // Nichts zu tun.
    }
    
    /**
     * Inits the DicomRouterPlugin with the specified Properties.
     * @param p  Properties containing the configuration of the plugin
     */
    public void init(Properties p) {
        String s;
        File newDirectory;
        
        // Properties lokal speichern
        props = p;
        
        // Directory
        s = props.getProperty("directory");
        
        if (s != null) {
            if ((newDirectory = Util.uriToFile(s)) != null) {
                directory = newDirectory;
            }
        }
        
        // Extension of file
        s = props.getProperty("extension");
        
        if (s != null) {
            extension = s;
        }
        
        // Separate_patients-Option nach Properties aendern
        s = props.getProperty("separate-patients");
        
        if (s != null) {
            if (s.toLowerCase().charAt(0) == 't') {
                separate_patients = true;
            } else {
                separate_patients = false;
            }
        }
        
        // Name of file
        // Wird in setDynamicProperties gesetzt, da der Wert vom aktuellen
        // Datset abhaengig ist.
        // Transfersyntax nach Properties aendern
        s = props.getProperty("transfersyntax");
        
        if (s != null) {
            if (s.charAt(0) == '$') {
                transfersyntax = s.substring(1);
            } else {
                transfersyntax = s;
            }
        }
        
        // Use_subdirectory-Option nach Properties aendern
        s = props.getProperty("use-subdirectory");
        
        if (s != null) {
            use_subdirectory = s;
        }
        
        // Write_dir_tree-Option nach Properties aendern
        s = props.getProperty("write-dir-tree");
        
        if (s != null) {
            if (s.toLowerCase().charAt(0) == 't') {
                write_dir_tree = true;
            } else {
                write_dir_tree = false;
            }
        }
        
        // File Meta Information Block nach Properties aendern
        s = props.getProperty("write-fmi");
        
        if (s != null) {
            if (s.toLowerCase().charAt(0) == 't') {
                write_fmi = true;
            } else {
                write_fmi = false;
            }
        }
    }
    
    /**
     * Sets dynamic properties, which depend on the actual Dataset to process.
     * @param ds  The Dataset to process.
     * @return True, if dynamic property could be set.
     */
    protected boolean setDynamicProperties(Dataset ds) {
        String s;
        
        // Name of file
        s = props.getProperty("filename");
        
        if (s != null) {
            filename = Util.getTagStringOrValue(s, ds);
            if (filename == null) {
                log.error("Can't find Element" + s.substring(1) +  " in Dataset.");
                return false;
            }
        }
        
        // Kein Fehler aufgetreten
        return true;
    }
    
    /**
     * Returns a version string.
     *
     * @return The version string
     */
    public String getVersion() {
        return VERSION;
    }

    
    /**
     * Closes the Plugin.
     */
    public void close() {
        // Nichts zu tun.
    }
    
    /**
     * Contains the proccesing of this plugin. This implementation handels all
     * exeptions inside the method and sends logging information about the exeption.
     * 
     * 
     * 
     * @param dataset The Dataset to process.
     * @return If succesfullCONTINUEN, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int process(Dataset dataset) {
        File    parent = null;
        File    imageFile = null;
        
        // Nur dann weitermachen, wenn ein Datset vorhanden ist
        if (dataset == null) {
            log.warn("No Dataset given.");
            
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Dynamische Properties setzen
        if (!setDynamicProperties(dataset)) {
            log.error("Dataset not saved.");
            
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Das Working Directory festlegen
        parent = directory;
        if (separate_patients) {
            parent = Util.addPatientDirectory(parent, dataset);
        }
        
        // Ein Fileobjekt erstellen
        if ((imageFile = createFilePathToSave(parent, dataset)) == null) {
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Image File exportieren
        boolean success = saveDataset(dataset, imageFile);
        
        if (!success) {
            return REQUEST_ABORT_RECEIVER;
        } else {
            return CONTINUE;
        }
    }
    
    /**
     * Create the file path of the file to save in the directory parent
     * depending of the property use_subdirectory.
     * @param parent the working directory.
     * @param ds the dataset to analyse.
     * @return the file.
     */
    protected File createFilePathToSave(File parent, Dataset ds) {
        File path;
        
        if (use_subdirectory != null) {
            if (! use_subdirectory.equals("")) {
                parent = new File(parent, use_subdirectory);
            }
        }
        
        if (write_dir_tree) {
            path = new File(parent, Util.datasetToTreeFileID(ds));
        } else {
            // Direkt in das angegebene Verzeichnis speichern
            path = new File(parent, filename + "." + extension);
        }
        
        return path;
    }
    
    /**
     * Saves the Dataset to the local filesystem.
     *
     * @param ds the Dataset to save.
     * @param f the File to which the Dataset should be save.
     */
    public boolean saveDataset(Dataset ds, File f) {
        FileOutputStream fos = null;
        DcmEncodeParam param = null;
        FileMetaInfo fmi;
        
        try {
            if (f.exists()) {
                // File besteht bereits. Erst loeschen, dann neu erstellen
                f.delete();
                f.createNewFile();
            } else {
                if (!f.getParentFile().exists()) {
                    // Directory Pfad existiert noch nicht
                    f.getParentFile().mkdirs();
                }
            }
            
            fos = new FileOutputStream(f);
            param = DcmEncodeParam.valueOf(UIDs.forName(transfersyntax));
            
            // File mit/ohne File Meta Information Block schreiben
            if (write_fmi) {
                fmi = DcmObjectFactory.getInstance().newFileMetaInfo(ds,
                        UIDs.forName(transfersyntax));
                ds.setFileMetaInfo(fmi);
                
                // Die Methode Dataset#writeFile schreibt -sofern != null- einen
                // File Meta Information Block und ruft dann Dataset#writeDataset
                // mit den selben Parametern auf.
                ds.writeFile(fos, param);
                
                // File Meta Information Block wieder loeschen
                ds.setFileMetaInfo(null);
            } else {
                ds.writeDataset(fos, param);
            }
            
            // Plugin ohne Fehler beendet
            if (log.isInfoEnabled()) {
                log.info("Dataset saved as file: " + f.getPath());
            }
            
            return true;
        } catch (Exception e) {
            log.error("Can't save file - Exception: " + e.getMessage());
            
            return false;
        } finally {
            try {
                fos.close();
            } catch (Exception ignore) {}
        }
    }
}

/**
 * Revisions:
 *
 * 2006.04.24, hacklaender: Property name replaced by filename
 */
