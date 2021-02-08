/*
 *  TagModifyPlugin.java
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
import de.iftm.dcm4che.router.util.*;

import org.apache.log4j.*;

import org.dcm4che.data.*;

import org.dcm4che.dict.*;

import java.util.*;


/**
 * This plugin modifies named elements of the submitted Dataset. It
 * supports the properties which start with the key <span
 * style="font-style: italic;">TagModifyPlugin </span>and  a subkeys,
 * which is the name of a DICOM element as given in  the Tag-dictionary.
 * The name may start with the escape-character '$',  which is ignored.<br>
 * The properties have no default values.<br>
 * <br>
 * If the value of the property starts with the character '#' the&nbsp;
 * value is interpreted as a command. Supported command are:<br>
 * <br>
 * <span style="font-style: italic;"> #remove</span><br>
 * The named element is removed from the dataset.<br>
 * <br>
 * <span style="font-style: italic;"> #clear</span><br>
 * The contents of the named element is cleared.<br>
 * <br>
 * <br>
 * If the value of the property starts with the character '$' the value is
 * interpreted as a name of a DICOM element. The contents of the element
 * named in the key is replaced by the contents of the element named in
 * the value field.<br>
 * <br>
 * If the value of the property does not starts with the character&nbsp;
 * '#', '@' or '$' its contents is replaced by the value string.<br>
 * <br>
 * @author Thomas Hacklaender
 * @version 2005.01.27
 */
public class TagModifyPlugin extends DicomRouterPlugin {
    /** The version string */
    final String VERSION = "2006.11.10";
    
    /** The logger for this plugin */
    Logger log = Logger.getLogger(TagModifyPlugin.class);
    
    /** The properties of this plugin */
    Properties props = null;
    
    /**
     * Constructor.
     */
    public TagModifyPlugin() {
        // Nichts zu tun.
    }
    
    /**
     * Inits the DicomRouterPlugin with the specified Properties.
     * @param p  Properties containing the configuration of the plugin
     */
    public void init(Properties p) {
        // Properties lokal speichern
        props = p;
        
        // Alle Properties koennen vom aktuellen Dataset abhaengig sein und
        // werden deshlab direkt in handleProcessNext ausgewertet.
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
        String  key;
        String  value;
        String  s;
        int     tag = 0;
        
        // Nur dann weitermachen, wenn ein Datset vorhanden ist
        if (dataset == null) {
            log.warn("No Dataset given.");
            
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Alle Properties bearbeiten
        for (Enumeration p = props.propertyNames(); p.hasMoreElements();) {
            key = (String) p.nextElement();
            value = props.getProperty(key);
            
            // Zu modifizierden Tag festlegen
            tag = Util.getTagFromPropertyString(key);
            if (tag <= 0) {
                log.error("Can't find tag in property key: " + key);
                return REQUEST_ABORT_RECEIVER;
            }
            
            // Kommandos in value auswerten
            if (value.charAt(0) == '#') {
                
                // Value ist ein Kommando
                if (value.equals("#clear")) {
                    // Element entfernen
                    dataset.remove(tag);
                    
                    // Leeres Element einfuegen
                    dataset.putXX(tag);
                    
                    if (log.isInfoEnabled()) {
                        log.info("Value of tag " + " (" + Tags.toHexString(tag, 8) + ") cleared.");
                    }
                } else if (value.equals("#remove")) {
                    // Element entfernen
                    dataset.remove(tag);
                    
                    if (log.isInfoEnabled()) {
                        log.info("Value of tag " + " (" + Tags.toHexString(tag, 8) + ") removed.");
                    }
                } else {
                    // Unbekanntes Kommandos
                    log.warn("Unknown command: " + value);
                    return REQUEST_ABORT_RECEIVER;
                }
                
                continue;
            }
            
            // Inhalt soll durch neuen Inhalt ersetzt werden
            
            s = Util.getTagStringOrValue(value, dataset);
            dataset.putXX(tag, s);
            
            if (log.isInfoEnabled()) {
                log.info("Value of tag " + " (" + Tags.toHexString(tag, 8) + ") replaced with " + value);
            }
            
        }
        
        // Plugin ohne Fehler beendet
        return CONTINUE;
    }
}
