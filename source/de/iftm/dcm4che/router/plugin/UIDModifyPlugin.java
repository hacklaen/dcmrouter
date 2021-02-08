/*
 *  UIDModifyPlugin.java
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

import de.iftm.dcm4che.router.property.DicomRouterProperties;

import org.apache.log4j.*;

import org.dcm4che.*;

import org.dcm4che.data.*;

import org.dcm4che.dict.*;

import org.dcm4che.util.*;

import java.util.*;


/**
 * This plugin modifies the UID's. The following keys are supported:<br>
 * <br>
 * <span style="font-style: italic;">SOPInstanceUID</span><br>
 * Default value:  #newroot<br>
 * <br>
 * <span style="font-style: italic;">StudyInstanceUID</span><br>
 * Default value:  #newroot<br>
 * <br>
 * <span style="font-style: italic;">SeriesInstanceUID</span><br>
 * Default value:  #newroot<br>
 * <br>
 * <br>
 * The property value describes how to modify the UID. There are three
 * commands:<br>
 * <br>
 * <span style="font-style: italic;">#original</span><br>
 * The UID is not changed.<br>
 * <br>
 * <span style="font-style: italic;">#new</span><br>
 * A new UID is generated using  the root UID of the dcm4che library.<br>
 * <br>
 * <span style="font-style: italic;">#newroot</span><br>
 * The new UID is  constructed from the root UID of the dcm4che library as
 * a praefix and  the remainder of the original UID as a postfix. The
 * length of the  remainder is choosen in that way, that the length of the
 * resulting UID  does not exceed 64 characters.<br>
 * <br>
 * @author Thomas Hacklaender
 * @version 2003.11.14
 */
public class UIDModifyPlugin extends DicomRouterPlugin {
    /** The version string */
    final String VERSION = "2006.11.10";
    
    /** The logger for this plugin */
    Logger log = Logger.getLogger(UIDModifyPlugin.class);
    
    /** The properties of this plugin */
    Properties props = null;
    
    /** DOCUMENT ME! */
    String sopCommand = "#newroot";
    
    /** DOCUMENT ME! */
    String studyCommand = "#newroot";
    
    /** DOCUMENT ME! */
    String seriesCommand = "#newroot";
    
    /** The root UID. Default is the root UID of the dcm4che library. */
    String rootUID = Implementation.getClassUID();
    
    
    /**
     * Constructor.
     */
    public UIDModifyPlugin() {
        // Nichts zu tun.
    }
    
    /**
     * Inits the DicomRouterPlugin with the specified Properties.
     * @param p  Properties containing the configuration of the plugin
     */
    public void init(Properties p) {
        String key;
        String value;
        
        // Properties lokal speichern
        props = p;
        
        // Alle Properties bearbeiten
        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            key = (String) e.nextElement();
            value = props.getProperty(key);
            
            // root-uid
            if (key.equals("root-uid")) {
                rootUID = value;
            }
            
            // Kommandos einlesen
            if (key.equals("$SOPInstanceUID") || key.equals("SOPInstanceUID")) {
                sopCommand = value;
            }
            
            if (key.equals("$StudyInstanceUID") || key.equals("StudyInstanceUID")) {
                studyCommand = value;
            }
            
            if (key.equals("$SeriesInstanceUID") || key.equals("SeriesInstanceUID")) {
                seriesCommand = value;
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
     * @return If succesfullCONTINUEN, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int process(Dataset dataset) {
        String origSOPInstanceUID;
        String origStudyInstanceUID;
        String origSeriesInstanceUID;
        
        // Originale UID's einlesen
        try {
            origSOPInstanceUID = dataset.getString(Tags.SOPInstanceUID);
            origStudyInstanceUID = dataset.getString(Tags.StudyInstanceUID);
            origSeriesInstanceUID = dataset.getString(Tags.SeriesInstanceUID);
        } catch (Exception e) {
            log.error("Can't read UID's - Exception: " + e.getMessage());
            
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Neue UID schreiben
        dataset.putUI(Tags.SOPInstanceUID,
                modifyUID(origSOPInstanceUID, sopCommand));
        
        if (log.isInfoEnabled()) {
            log.info("SOPInstanceUID modified. Command: " + sopCommand);
        }
        
        dataset.putUI(Tags.StudyInstanceUID,
                modifyUID(origStudyInstanceUID, studyCommand));
        
        if (log.isInfoEnabled()) {
            log.info("StudyInstanceUID modified. Command: " + studyCommand);
        }
        
        dataset.putUI(Tags.SeriesInstanceUID,
                modifyUID(origSeriesInstanceUID, seriesCommand));
        
        if (log.isInfoEnabled()) {
            log.info("SeriesInstanceUID modified. Command: " + seriesCommand);
        }
        
        // Plugin ohne Fehler beendet
        return CONTINUE;
    }
    
    /**
     * Modifies a UID depending on a command.
     * @param origUID The UID found in the Dataset.
     * @param cmd The command for modification. Possible values are "#original"
     *            "#new" and "#newroot".
     * @return the new UID.
     */
    private String modifyUID(String origUID, String cmd) {
        String remainder;
        int maxLength;
        
        if (cmd.equals("#original")) {
            
            // Keine Veraenderung notwendig
            return origUID;
            
        } else if (cmd.equals("#new")) {
            
            // Neue UID generieren
            return UIDGenerator.getInstance().createUID();
            
        } else if (cmd.equals("#newroot")) {
            
            if ((rootUID.length() + origUID.length() + 1) <= 64) {
                return rootUID + "." + origUID;
            } else {
                maxLength = 64 - rootUID.length() - 1;
                remainder = origUID.substring(origUID.length() - maxLength);
                
                if (remainder.charAt(0) == '.') {
                    remainder = remainder.substring(1);
                }
                
                return rootUID + "." + remainder;
            }
            
        } else {
            // Unbekanntes Kommandos
            log.warn("UID not changed. Unknown command: " + cmd);
            
            return origUID;
        }
    }
}
