/*
 *  DicomStorageSCUPlugin.java
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
import de.iftm.dcm4che.services.CDimseService;
import de.iftm.dcm4che.services.ConfigProperties;

import org.apache.log4j.Logger;

import org.dcm4che.data.*;

import org.dcm4che.net.*;

import org.dcm4che.util.*;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.ParseException;


/**
 * This class extends the {@link DicomRouterPlugin} class and implements
 * {@link DicomRouterPlugin} interface. It take the rolle of a DICOM Storage
 * Service Class User and sends Dicom Object to a DICOM Storage
 * Service Class Provider.
 * <br><br>
 * 2005.08.20 hacklaender: Changed the processing of properties.<br>
 * 2004.01.31 hacklaender: Logging messages modified: No stack trace.<br>
 */
public class DicomStorageSCUPlugin extends DicomRouterPlugin {
    
    /** DOCUMENT ME! */
    private static Logger log = Logger.getLogger(DicomStorageSCUPlugin.class);
    
    /** DOCUMENT ME! */
    private final static String VERSION = "2006.11.10";
    
    /** The configuration properties of the StorageService working class. */
    ConfigProperties cfg = null;
    
    
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>  Local fields defined by properties: >>>>>>>
    
    /** The transmission protocol to use.
     * <p>Normally the 'dicom' protocol is used. The data are send unencrypted.
     * If you would like to use the encrypted transmission, use the 'dicom-tls'
     * protocol. If you use it, you have to define some other properties, named 'tls_xxx'.
     * <p>dicom-tls: accept TLS connection (offer AES and DES encryption)
     * <p>dicom-tls.aes : accept TLS connection (force AES or DES encryption)
     * <p>dicom-tls.3des: accept TLS connection (force DES encryption)
     * <p>dicom-tls.nodes : accept TLS connection (no encryption) */
    private String protocol = "dicom";
    
    /** Own AET (Application Entity Title).
     * Default is "<any>", that means association of any AET is accepted.
     * The provided property value should be a comma or space separated list of individual AETs.
     * Providing no value is equivalent to to a value "<any>". */
    private String called_aets = "DCMRCV";
    
    /** AETs  (Application Entity Titles) of the storage service users.
     * Default is "<any>", that means association of any AET is accepted.
     * The provided property value should be a comma or space separated list of individual AETs.
     * Providing no value is equivalent to to a value "<any>". */
    private String calling_aets = "DCMSND";
    
    /** The IP address of the computer the Dataset should be stored. 127.0.0.1 corresponds to "localhost". */
    private String host = "127.0.0.1"; // corresponds to localhost
    
    /** Holds the port number of the server [default=104]. */
    private String port = "104";
    
    
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    
    
    /**
     * Creates a DicomSendPlugin Object.
     */
    public DicomStorageSCUPlugin() {
    }
    
    
    /**
     * Inits the DicomRouterPlugin with the specified Properties.
     * @param p  Properties containing the configuration of the plugin
     */
    public void init(Properties p) {
        String s;
        URL url;
        
        // Read the default configuration properties for the working class StorageService
        try {
            url = CDimseService.class.getResource("resources/CDimseService.cfg");
            cfg = new ConfigProperties(url);
        } catch (Exception e) {
            log.error("Can't load default configuration file from resource.");
        }
        
        if (props == null) {
            // nichts zu tun
            return;
        }
        
        // called_aets
        s = p.getProperty("called-aets");
        if (s != null) {
            called_aets = s;
        }
        
        // calling_aets
        s = p.getProperty("calling-aets");
        if (s != null) {
            calling_aets = s;
        }
        
        // host
        s = p.getProperty("host");
        if (s != null) {
            host= s;
        }
        
        // port
        s = p.getProperty("port");
        if (s != null) {
            port = s;
        }
        
        // protocol
        s = p.getProperty("protocol");
        if (s != null) {
            protocol = s;
        }
        
        // cdimse_cfg_uri
        // Two forms are possible:
        // 1. Absolute reference (file:/C:/a/b/c.txt): The reference is taken as is.
        // 2. Relative reference (b/c.txt): The referece is relative to the package of the class de.iftm.dcm4che.services.StorageService
        s = props.getProperty("cdimse-cfg-uri");
        if (s != null) {
            try {
                url = ConfigProperties.fileRefToURL(CDimseService.class.getResource(""), s);
                cfg = new ConfigProperties(url);
            } catch (Exception e) {
                log.error("Error in cdimse_cfg_uri. Default values used.");
            }
        }
        
    }
    
    /**
     * Sends the Dicom Object to Dicom Stroge Service Class Provider.
     *
     * @param dataset holds the value of the Dicom Object
     * @return if succesfull CONTINUE, REQUEST_ABORT_RECEIVER or CONTINUE_CHAIN if an error occurs
     */
    protected int process(Dataset dataset) {
        DcmURL  dcmURL;
        CDimseService cDimseService;
        boolean     success = false;
        
        // Update the configuration properties with properties given to this class
        cfg.put("host", host);
        cfg.put("port", port);
        if (called_aets != null) cfg.put("called-aets", called_aets);
        if (calling_aets != null) cfg.put("calling-aets", calling_aets);
        
        // Create the destination dcmURL: PROTOCOL://CALLED[:CALLING]@HOST[:PORT]
        dcmURL = new DcmURL(protocol, cfg.getProperty("called-aets"), cfg.getProperty("calling-aets"), cfg.getProperty("host"), Integer.parseInt(cfg.getProperty("port")));
        
        // Create working class CDimseService
        try {
            cDimseService = new CDimseService(cfg, dcmURL);
        } catch (ParseException e) {
            log.error("Error creating CDimseService");
            // Do not abort plugin queue
            return CONTINUE;
        }
        
        // Open association
        try {
            if (! cDimseService.aASSOCIATE()) {
                log.error("Can't associate");
                // Do not abort plugin queue
                return CONTINUE;
            }
        } catch (Exception e) {
            log.error("Error while associating");
            // Do not abort plugin queue
            return CONTINUE;
        }
        
        // Store Dataset
        try {
            cDimseService.cSTORE(dataset);
        } catch (Exception e) {
            log.error("Error while sending Dataset");
            // Do not abort plugin queue
            return CONTINUE;
        }
        
        // Release association
        try {
            // Wait until it receives the responds to the release request.
            cDimseService.aRELEASE(true);
        } catch (Exception e) {
            log.error("Error while releasing association");
            // Do not abort plugin queue
            return CONTINUE;
        }
        
        // Successful stored
        return CONTINUE;
    }

    
    /**
     * Closes the Association with a release.
     */
    public void close() {
    }
    
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getVersion() {
        return VERSION;
    }
}


/*
 * $Log: DicomStorageSCUPlugin.java,v $
 * Revision 1.8  2004/02/12 18:18:24  k_kleber
 * Weiteres Umbenennen der Rückgabewerte der Methode process() in DicomRouterPlugin
 *
 * Revision 1.7  2004/02/12 17:55:29  k_kleber
 * Überarbeiten der Schnittstelle für das DicomRouterPlugin.
 * -Zusammenlegen der abstracten Klasse DefaultDicomRouter mit DicomRouterPlugin
 * -Umbennen der abstacten Methodennamen
 *
 */
