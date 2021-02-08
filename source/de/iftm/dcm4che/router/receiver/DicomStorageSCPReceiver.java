/*
 *  Copyright (c) 2006 by
 *
 *  Dr. Thomas Hacklaender @ IFTM Institut für Telematik in der Medizn GmbH
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

import java.io.*;
import java.util.*;
import java.net.*;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import de.iftm.dcm4che.router.property.*;
import de.iftm.dcm4che.router.plugin.DicomRouterPlugin;
import de.iftm.dcm4che.router.plugin.PluginChain;
import de.iftm.dcm4che.router.util.DicomRouterException;
import de.iftm.dcm4che.router.util.DicomRouterEvent;

import de.iftm.dcm4che.services.ConfigProperties;
import de.iftm.dcm4che.services.StorageService;
import de.iftm.dcm4che.services.StorageServiceEvent;
import de.iftm.dcm4che.services.StorageServiceEventListener;

import org.dcm4che.data.*;
import org.dcm4che.net.*;


/**
 * This class initializes a DICOM serverand receives incoming DICOM objects.
 * To be able to adopt this fuctionality to upcoming versions of the dcm4che
 * package most of the functionality is done by the sample program <CODE>DcmRcv</CODE>
 * coming with dcm4che.
 *
 * <p><CODE>DcmRcv</CODE> is fully configured by an object of Configuration. It
 * creates an instance of <CODE>Server</CODE> and start the server. Inside the
 * <CODE>initPresContext</CODE> method the instance itself is registerd as a
 * <CODE>DcmServiceBase</CODE> of the server.
 *
 * <p>The <CODE>Server</CODE> implements a thread-pool and assigns every association
 * with a separat thread. Each thread calls the handlers of the service elements
 * defined in the registered <CODE>DcmServiceBase</CODE> classes.
 *
 * <p>As the <CODE>DcmRcv</CODE> overrides the <CODE>doCStore</CODE> method of the
 * default <CODE>DcmServiceBase</CODE> implementation inside dcm4che all received
 * objects are send to the <CODE>doCStore</CODE> methode of <CODE>DcmRcv</CODE>.
 *
 * <p>To avoid modifying DcmRcv to mutch, the functionality of <CODE>doCStore</CODE>
 * methode is replaced by a call to <CODE>DicomStorageSCPReceiver::doCStore()</CODE>,
 * which actually handels the processing.
 * @author thacklaender
 * @version 2006.04.24
 */
public class DicomStorageSCPReceiver extends DicomReceiver implements StorageServiceEventListener {
    
    /** The version string */
    public final static String VERSION = "2006.11.10";
    
    /** The working class. */
    StorageService storageService = null;
    
    /** The configuration properties of the StorageService working class. */
    ConfigProperties cfg = null;
    
    /** Holds the chain of plugins. */
    private PluginChain pluginChain = null;
    
    
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
    
    /** Holds the port number of the server [default=104]. */
    private String port = "104";
    
    /** Own AET (Application Entity Title).
     * Default is "<any>", that means association of any AET is accepted.
     * The provided property value should be a comma or space separated list of individual AETs.
     * Providing no value is equivalent to to a value "<any>". */
    private String called_aets = null;
    
    /** AETs  (Application Entity Titles) of the storage service users.
     * Default is "<any>", that means association of any AET is accepted.
     * The provided property value should be a comma or space separated list of individual AETs.
     * Providing no value is equivalent to to a value "<any>". */
    private String calling_aets = null;
    
    
    /**
     * Creates a new instance of DicomStorageSCPReceiver
     * @param props the properties for the receiver and plugins.
     */
    public DicomStorageSCPReceiver(Properties props) {
        super(props);
        
        // Initialise
        init();
    }
    
    
    /**
     * Creates a new instance of DicomStorageSCPReceiver
     * @param uri the URI of the properties file for the receiver and plugins.
     */
    public DicomStorageSCPReceiver(String uri) throws IOException {
        super(uri);
        
        // Initialise
        init();
    }
    
    
    /**
     * Creates a new instance of Class
     * @param argv the arguments passed to the programs main method.
     */
    public DicomStorageSCPReceiver(String[] argv) {
        super(argv);
        
        // Initialise
        init();
        
        // Start automatically
        start();
    }
    
    
    //>>>> Static methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    
    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        new DicomStorageSCPReceiver(argv);
    }
    
    
    //>>>> Public methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    
    
    /**
     * Copies the relevant properties to local fields.
     */
    protected void init() {
        String  s;
        File f;
        URL url;
        
        // Use properties set in the parent class DicomReceiver
        Properties props = drProperties.extractReceiverProperties();
        
        // Read the default configuration properties for the working class StorageService
        try {
            url = StorageService.class.getResource("resources/StorageService.cfg");
            cfg = new ConfigProperties(url);
        } catch (Exception e) {
            log.error("Can't load default configuration file from resource.");
        }
        
        if (props == null) {
            // nichts zu tun
            return;
        }
        
        // called_aets
        s = props.getProperty("called-aets");
        if (s != null) {
            called_aets = s;
        }
        
        // calling_aets
        s = props.getProperty("calling-aets");
        if (s != null) {
            calling_aets = s;
        }
        
        // port
        s = props.getProperty("port");
        if (s != null) {
            // port speichern
            port = s;
        }
        
        // protocol
        s = props.getProperty("protocol");
        if (s != null) {
            // protocol speichern
            protocol = s;
        }
        
        // storage_cfg_uri
        // Two forms are possible:
        // 1. Absolute reference (file:/C:/a/b/c.txt): The reference is taken as is.
        // 2. Relative reference (b/c.txt): The referece is relative to the package of the class de.iftm.dcm4che.services.StorageService
        s = props.getProperty("storage-cfg-uri");
        if (s != null) {
            try {
                url = ConfigProperties.fileRefToURL(StorageService.class.getResource(""), s);
                cfg = new ConfigProperties(url);
            } catch (Exception e) {
                log.error("Error in storage-cfg-uri. Default values used.");
            }
        }
        
    }
    
    
    /**
     *
     */
    public boolean start() {
        DicomRouterEvent dre;
        
        // Info
        if (log.isInfoEnabled()) {
            log.info("Version: " + this.VERSION);
        }
        
        try {
            pluginChain = new PluginChain(drProperties);
        } catch (DicomRouterException e) {
            log.error(e.getMessage());
            return false;
        }
        
        pluginChain.initPlugins();
        
        // Update the configuration properties with properties given to this class
        cfg.put("protocol", protocol);
        cfg.put("port", port);
        if (called_aets != null) cfg.put("called-aets", called_aets);
        if (calling_aets != null) cfg.put("calling-aets", calling_aets);
        
        // Initialize the working class with the actual configuration properties.
        try {
            storageService = new StorageService(cfg);
        } catch (ParseException e) {
            log.error("Can't configure Storage SCP.");
            return false;
        }
        
        // Register to server as a listener for StorageServiceEvents
        storageService.addStorageServiceClassEventListener(this);
        
        // Notify listeners: STARTING_EVENT
        dre = new DicomRouterEvent(this);
        dre.setStartingEvent();
        fireDicomRouterEvent(dre);
        
        // Start the instance of the working class
        try {
            storageService.start();
        } catch (IOException e) {
            log.error("Can't start Storage SCP.");
            return false;
        }
        
        return true;
    }
    
    
    /**
     *
     */
    public void stop() {
        DicomRouterEvent dre;
        
        storageService.stop();
        pluginChain.closePlugins();
        
        // Notify listeners: FINISHED_EVENT
        dre = new DicomRouterEvent(this);
        dre.setFinishedEvent();
        fireDicomRouterEvent(dre);
    }
    
    
    /**
     * Handles StorageServiceEvents.
     * @param evt the event to hanle.
     */
    public void handleStorageServiceEvent(StorageServiceEvent evt) {
        int     chainResultCode;
        Dataset ds;
        DicomRouterEvent dre;
        
        ds = evt.getDataset();
        
        // Notify listeners: NEXT_OBJECT_EVENT
        dre = new DicomRouterEvent(this);
        dre.setNextObjectEvent(ds);
        fireDicomRouterEvent(dre);
        
        // Process Dataset
        chainResultCode = pluginChain.process(ds);
        
        if (chainResultCode != DicomRouterPlugin.CONTINUE) {
            // Abort processingthe DICOM object set
            stop();
        }
        
    }
    
}

/**
 * Revisions:
 *
 * 2006.11.10, hacklaender: Added DicomRouterEvent's.
 * 2006.06.24, hacklaender: Replaced working class with StorageService.
 * 2006.04.24, hacklaender: Added main method.
 */
