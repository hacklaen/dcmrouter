/*
 *  DefaultDicomRouterPlugin.java
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

import org.apache.log4j.Logger;

import de.iftm.dcm4che.router.property.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.Tags;

import java.util.Properties;
import java.lang.reflect.*;


/**
 * Implements a DicomRouterPlugin.
 * The abortion or closing of the plugins have to take place by calling methods close or abort.
 * Before a DefaultDicomRouterPlugin can be closed the next plugin in the chain plugin has to be closed.
 * A abort of a DefaultDicomRouterPlugin is only possible if the plugin is not closed.
 */
public abstract class DicomRouterPlugin {
    
    /** DOCUMENT ME! */
    private static Logger log = Logger.getLogger(DicomRouterPlugin.class);

    
    /** Return value for process. */
    public static final int CONTINUE = 0;
    
    /** Return value for process. */
    public static final int REQUEST_ABORT_PLUGIN_CHAIN = 1;
    
    /** Return value for process. */
    public static final int REQUEST_ABORT_RECEIVER = 2;

    /** Properties of the plugin */
    Properties props = null;
    
    /**Holds value of property nextPluginInChain. */
    private DicomRouterPlugin nextPluginInChain = null;
 
    /** Holds value of property closed. */
    private boolean closed = false;
    
    
    //>>>> Abstract methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
   
    /**
     * Inits the DicomRouterPlugin with the specified Properties.
     * @param p  Properties containing the configuration of the plugin
     */
    public abstract void init(Properties p);
   
    /**
     * Contains the actualy processing of these DicomRouterPlugin
     * @param dataset Contains Dicom Objec
     * @return CONTINUE, if succesfull, REQUEST_ABORT_RECEIVER if the next plugin
     * in the chain should not be executed, else REQUEST_ABORT_PLUGIN_CHAIN
     */
    protected abstract int process(Dataset dataset);
    
    /**
     * Closes plugin
     * @throws Exception  Bei auftreten eines Fehlers
     */
    protected abstract void close();
    
    /**
     * Returns a String with the version id of the plugin
     * @return  the version string.
     */
    public abstract String getVersion();    

    
    //>>>> Static methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
     
    /**
     * Creates an instance of the plugin with the name of the plugin given as a
     * String and initializes the plugin with the given prperties.
     * Throws Exception if such plugin doesn´t exist.
     * 
     * @param pluginName the fully qualified name of the plugin as a String
     * @return Instance of {@link DicomRouterPlugin}
     */
    public static DicomRouterPlugin createByName(String pluginName) {
        Class             theClass;
        Constructor       theClassConstructor;
        Object[]          obj;
        DicomRouterPlugin plugin;
        
        try {
            theClass = Class.forName(pluginName);
        } catch (Exception e) {
            log.error("Can't load plugin: " + pluginName.toString());
            
            return null;
        }
        
        try {
            theClassConstructor = theClass.getConstructor(new Class[0]);
        } catch (NoSuchMethodException e) {
            log.error("Can't get constructor of plugin: " + pluginName.toString());
            
            return null;
        }
        
        try {
            obj = new Object[0];
            plugin = (DicomRouterPlugin) theClassConstructor.newInstance(obj);
        } catch (Exception e) {
            log.error("Can't instantiate plugin: " + pluginName.toString());
            
            return null;
        }
        
        log.info("Plugin " + pluginName + " successful created");
        
        return plugin;
    }
    

    //>>>> Public methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    
    /**
     * Stores the properties of the plugin inside the instance of the class.
     *@param p the properties.
     */
    public void setProperties(Properties p) {
        props= p;
    }
    
    
    /**
     * Gets the properties of the plugin stored inside the instance of the class.
     *@return the properties.
     */
    public Properties getProperties() {
        return props;
    }
    
 }


/*
 * Revisions:
 *
 * 2006.04.26, hacklaender: Rewritten.
 */

