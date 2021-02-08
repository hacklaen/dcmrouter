/*
 *  SaveDicomdirPlugin.java
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
import de.iftm.dcm4che.services.ConfigProperties;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.media.*;
import org.dcm4che.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.*;


/**
 * This plugins saves the given Dataset as a file to the local filesystem
 * and stores a reference to this file in a DICOMDIR index file according
 * DICOM Part 10. It supports the properties which start with the key
 * SaveFilesystemPlugin. For description of the subkeys (= properties) see
 * the user manual.<br>
 * <br>
 * The file uses the "resources/SaveDicomdirPlugin.cfg" for configuration.<br>
 * <br>
 * 2005.08.17, th: Loading of conifuration parameters by class DicomRouterProperties.
 *
 * @author Thomas Hacklaender
 * @version 2005.08.17
 */
public class SaveDicomdirPlugin extends DicomRouterPlugin {
    /** The version string */
    final String VERSION = "2006.11.10";
    
    /** The logger for this plugin */
    Logger log = Logger.getLogger(SaveDicomdirPlugin.class);
    
    
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>  Local fields defined by properties: >>>>>>>
    
    
    /** The working directory to save the Dataset. */
    File directory = Util.uriToFile("./");
    
    /** If true, write files in the subdirectory "$PatientName_$PatientBirthDate/<directory>/" */
    boolean separate_patients = false;
    
    /** Write files in the subdirectory "[$PatientName_$PatientBirthDate/]<directory>/<use_subdirectory>/". */
    String use_subdirectory = "";
    
    /** If true:
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
     * Use a random string of 8 characters (containing 'A'..'Z', '0'..'9') as the file ID.
     */
    boolean write_dir_tree = true;
    
    /** Transfersyntax of the saved file. One of the strings ImplicitVRLittleEndian,
     * ExplicitVRLittleEndian, ExplicitVRBigEndian. The string may start with
     * the praefix-character '$'.
     */
    String transfersyntax = "ImplicitVRLittleEndian";
    
    
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    
    
    /** The DICOMDIR file */
    protected File dicomdir = null;
    
    /** DirBuilder preferences extracted from properties */
    protected DirBuilderPref dirBuilderPref = null;
    
    /** Default configuration loaded from resource file. */
    protected ConfigProperties cfg = null;
    
    
    /**
     * Constructor.
     */
    public SaveDicomdirPlugin() {
    }
    
    
    /**
     * Inits the DicomRouterPlugin with the specified Properties.
     * @param p  Properties containing the configuration of the plugin
     */
    public void init(Properties p) {
        String s;
        File newDirectory;
        
        // Read the configuration properties for the working class DcmSnd
        URL url = this.getClass().getResource("resources/SaveDicomdirPlugin.cfg");
        try {
            cfg = new ConfigProperties(url);
        } catch (IOException e) {
            log.error("Can't load default configuration file from resource.");
        }
        
        // dirBuider aus Konfigurationswerten erzeugen
        dirBuilderPref = getDirBuilderPref();
        
        // User Properties auswerten
        
        if (p == null) {
            // nichts zu tun
            return;
        }
        
        // Directory
        s = p.getProperty("directory");
        
        if (s != null) {
            if ((newDirectory = Util.uriToFile(s)) != null) {
                directory = newDirectory;
            }
        }
        
        // Separate_patients-Option nach Properties aendern
        s = p.getProperty("separate-patients");
        
        if (s != null) {
            if (s.toLowerCase().charAt(0) == 't') {
                separate_patients = true;
            } else {
                separate_patients = false;
            }
        }
        
        // Transfersyntax
        s = p.getProperty("transfersyntax");
        
        if (s != null) {
            if (s.charAt(0) == '$') {
                transfersyntax = s.substring(1);
            } else {
                transfersyntax = s;
            }
        }
        
        // Use_subdirectory-Option nach Properties aendern
        s = p.getProperty("use-subdirectory");
        
        if (s != null) {
            use_subdirectory = s;
        }
        
        // Write_dir_tree-Option nach Properties aendern
        s = p.getProperty("write-dir-tree");
        
        if (s != null) {
            if (s.toLowerCase().charAt(0) == 't') {
                write_dir_tree = true;
            } else {
                write_dir_tree = false;
            }
        }
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
     * @return if succesfull CONTINUE, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int process(Dataset dataset) {
        File                parent;
        File                imageFile;
        FileOutputStream    fos = null;
        FileMetaInfo        fmi;
        DcmEncodeParam      param = null;
        DirBuilder          dirBuilder = null;
        String              uid;
        DirWriter           dirWriter = null;
        
        // Nur dann weitermachen, wenn ein Datset vorhanden ist
        if (dataset == null) {
            log.warn("No Dataset given.");
            
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Das Working Directory festlegen
        parent = directory;
        if (separate_patients) {
            parent = Util.addPatientDirectory(parent, dataset);
        }
        
        // DICOMDIR als Indexfile definieren
        dicomdir = new File(parent, "DICOMDIR");
        
        try {
            // Einen DirWriter und DirBuilder oeffnen
            if (dicomdir.exists()) {
                
                // DICOMDIR besteht schon
                // Instanz eines DirWriter mit diesem DICOMDIR erzeugen
                dirWriter = DirBuilderFactory.getInstance().newDirWriter(dicomdir, null);
                
            } else {
                
                // DICOMDIR besteht noch nicht.
                // Es muss ein neuer File mit eigener UID erzeugt werden
                
                if (!dicomdir.getParentFile().exists()) {
                    // Directory Pfad zum DICOMDIR existiert noch nicht, erstellen
                    dicomdir.getParentFile().mkdirs();
                }
                
                // Neue UID erzeugen:
                uid = UIDGenerator.getInstance().createUID();
                
                // Instanz eines DirWriter mit neuem DICOMDIR erzeugen
                dirWriter = DirBuilderFactory.getInstance().newDirWriter(dicomdir, uid, null, null, null, null);
            }
            
            // DirBuilder erzeugen
            dirBuilder = DirBuilderFactory.getInstance().newDirBuilder(dirWriter, dirBuilderPref);
            
            // File fuer die Bilddatei erzeugen
            imageFile = createFilePathToSave(parent, dataset);
            
            if (imageFile.exists()) {
                
                // File besteht bereits:
                // Erst loeschen, dann neu erstellen
                imageFile.delete();
                imageFile.createNewFile();
                
            } else {
                
                // File besteht noch nicht:
                if (!imageFile.getParentFile().exists()) {
                    // Directory Pfad auf den File existiert noch nicht
                    // Erzeugen
                    imageFile.getParentFile().mkdirs();
                }
            }
            
            // Outputstream zum schreiben des Files erzeugen
            fos = new FileOutputStream(imageFile);
            // Parameterblock mit Transfersyntax erzeugen
            param = DcmEncodeParam.valueOf(UIDs.forName(transfersyntax));
            // File mit File Meta Information Block schreiben
            fmi = DcmObjectFactory.getInstance().newFileMetaInfo(dataset, UIDs.forName(transfersyntax));
            dataset.setFileMetaInfo(fmi);
            dataset.writeFile(fos, param);
            
            // Referenz auf den Image-File dem DICOMDIR hinzufuegen
            dirBuilder.addFileRef(imageFile);
            
        } catch (Exception e) {
            log.error("Can't save Dataset to DICOMDIR - Exception: " + e.getMessage());
            return REQUEST_ABORT_RECEIVER;
        } finally {
            // DirBuilder und implizit DirWriter schliessen
            try {
                dirBuilder.close();
            } catch (Exception ignore) {
            }
        }
        
        // Plugin ohne Fehler beendet
        if (log.isInfoEnabled()) {
            log.info("Dataset saved to DICOMDIR: " + dicomdir.toString());
        }
        
        return CONTINUE;
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
            path = new File(parent, randomUniqueFileID(parent));
        }
        
        return path;
    }
    
    /**
     * Create a unique random file ID containing 8 hexadecimal uppercase digits.
     * @param parent the directory where the file should be created.
     * @return the file ID.
     */
    protected String randomUniqueFileID(File parent) {
        String fileID;
        File file;
        
        do {
            fileID = randomFileID();
            file = new File(parent, fileID);
        } while(file.exists());
        
        return fileID;
    }
    
    /**
     * Create a random file name containing 8 hexadecimal uppercase digits.
     * @return the file ID string.
     */
    protected String randomFileID() {
        Random RND = new Random();
        
        return Util.stringToFileIDComponentString(Integer.toHexString(RND.nextInt()).toString());
    }
    
    
    //>>>>>>>>>>>>>>>>> Aus dcm4che Version 1.1.4, /samples/media/DcmDir.java
    
    private final static DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private final static DirBuilderFactory fact = DirBuilderFactory.getInstance();
    
    /**
     * Adds all lines with property key starting with "dir." to DirBuilderPref.
     * starting with "dir.".
     * @return the DirBuilderPref.
     */
    private DirBuilderPref getDirBuilderPref() {
        HashMap map = new HashMap();
        for (Enumeration en = cfg.keys(); en.hasMoreElements();) {
            addDirBuilderPrefElem(map, (String) en.nextElement());
        }
        DirBuilderPref pref = fact.newDirBuilderPref();
        for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            pref.setFilterForRecordType((String) entry.getKey(), (Dataset) entry.getValue());
        }
        return pref;
    }
    
    /**
     * Add one line of preferences (key starting with "dir.") to map.
     * @param map the map to add to.
     * @param key the key of the preference entry.
     */
    private void addDirBuilderPrefElem(HashMap map, String key) {
        if (!key.startsWith("dir.")) { return; }
        
        int pos2 = key.lastIndexOf('.');
        String type = key.substring(4, pos2).replace('_', ' ');
        Dataset ds = (Dataset) map.get(type);
        if (ds == null) {
            map.put(type, ds = dof.newDataset());
        }
        int tag = Tags.forName(key.substring(pos2 + 1));
        ds.putXX(tag, VRMap.DEFAULT.lookup(tag));
    }
    
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
}
