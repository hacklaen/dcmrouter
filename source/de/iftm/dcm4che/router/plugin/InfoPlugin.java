/*
 *  InfoPlugin.java
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

import org.dcm4che.data.*;

import org.dcm4che.dict.*;

import java.util.*;


/**
 * This plugin logs information of the processed dataset.<br>
 * <br>
 * It supports no properties.
 *
 * @author Thomas Hacklaender
 * @version 2003.11.14
 */
public class InfoPlugin extends DicomRouterPlugin {
    /** The version string */
    final String VERSION = "2006.11.10";

    /** The logger for this plugin */
    Logger log = Logger.getLogger(InfoPlugin.class);

    /**
    * Constructor.
    */
    public InfoPlugin() {
        // Nichts zu tun.
    }

    /**
    * Inits the DicomRouterPlugin with the specified Properties.
    * @param p  Properties containing the configuration of the plugin
    */
    public void init(Properties p) {
        // Fuer das Plugin sind keine Properties definiert
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
        String info;

        try {
            info = "Processing: ";
            info += dataset.getString(Tags.PatientName);
            info += " - ";
            info += dataset.getString(Tags.StudyID);
            info += "/";
            info += dataset.getString(Tags.SeriesNumber);
            info += "/";
            info += dataset.getString(Tags.InstanceNumber);
            info += "/";
            info += dataset.getString(Tags.AcquisitionDate);
            info += "-";
            info += dataset.getString(Tags.AcquisitionTime);
        } catch (Exception e) {
            log.error("Can't extract DICOM element: ", e);

            return REQUEST_ABORT_RECEIVER;
        }

        log.info(info);

        // Plugin ohne schwerwiegende Fehler beendet
        return CONTINUE;
    }
}
