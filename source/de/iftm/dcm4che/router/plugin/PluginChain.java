/*
 *  PluginChain.java
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

import org.apache.log4j.Logger;

import org.dcm4che.data.*;
import org.dcm4che.net.*;
import org.dcm4che.util.*;

import java.io.*;
import java.util.*;


/**
 * This class implements a factory for creating and linking {@link DicomRouterPlugin}. 
 * The PluginChain must contain at least one plugin, a chain with only one NullPlugin 
 * is sufficient.
 */
public class PluginChain {
    /** DOCUMENT ME! */
    Logger log = Logger.getLogger(PluginChain.class);
    
    /**  */
    private Vector pluginVector = null;
    
    
    /**
     * Creates new PluginChain
     * @param drProps Holds the configurationvalues for the plugins
     */
    public PluginChain(DicomRouterProperties drProps) throws DicomRouterException {
        createChain(drProps);
    }
    
    
    /**
     *
     */
    public void createChain(DicomRouterProperties drProps) throws DicomRouterException {
        int                     numberOfPlugins;
        int                     numberOfFirstPlugin;
        int                     numberOfLastPlugin;
        DicomRouterPlugin       nextPlugin;
        String                  nextPluginName;
        Properties              nextPluginProperties;
        int                     nextPluginIndex;
        
        numberOfPlugins = drProps.getNumberOfPlugins();
        if (numberOfPlugins == 0) {
            throw new DicomRouterException(DicomRouterException.NO_PLUGINS_SPECIFIED);
        }
        
        // Empty chain
        pluginVector = new Vector();
        
        numberOfFirstPlugin = drProps.getIndexNumberOfFirstPlugin();
        numberOfLastPlugin = drProps.getIndexNumberOfLastPlugin();
        if ((numberOfFirstPlugin == -1) || (numberOfLastPlugin == -1)) {
            throw new DicomRouterException(DicomRouterException.PLUGIN_INDEX_SYNTAX_ERROR);
        }
        
        log.info("Number of plugins            : " + numberOfPlugins);
        log.info("Index number of first plugin : " + numberOfFirstPlugin);
        log.info("Index number of last plugin  : " + numberOfLastPlugin);
        
        for (int i = 0; i < drProps.getPluginIndexNumbers().length; i++) {
            nextPluginIndex = drProps.getPluginIndexNumbers()[i];
            nextPluginProperties = drProps.extractPluginProperties(nextPluginIndex);
            
            if (nextPluginProperties.isEmpty()) {
                throw new DicomRouterException(DicomRouterException.NO_PROPERTIES_FOR_PLUGIN);
            }
            
            nextPluginName = (String) nextPluginProperties.getProperty("name");
            nextPlugin = DicomRouterPlugin.createByName(nextPluginName);
            nextPlugin.setProperties(nextPluginProperties);
            pluginVector.addElement(nextPlugin);
        }
    }
    
    
    /**
     *
     */
    public void initPlugins() {
        DicomRouterPlugin nextPlugin;
        
        // Process all plugins in sequence
        for (int i = 0; i < pluginVector.size(); i++) {
            nextPlugin = (DicomRouterPlugin) pluginVector.elementAt(i);
            
            nextPlugin.init(nextPlugin.getProperties());
            
        }
    }
    
    
    /**
     * @param ds the Dataset to process.
     */
    public final int process(Dataset ds) {
        DicomRouterPlugin   nextPlugin;
        int                 pluginResultCode;
        int                 chainResultCode = DicomRouterPlugin.CONTINUE;
        
        // Process all plugins in sequence
        process_chain_block: for (int i = 0; i < pluginVector.size(); i++) {
            nextPlugin = (DicomRouterPlugin) pluginVector.elementAt(i);
            
            try {
                // Catch unexpected exceptions
                pluginResultCode = nextPlugin.process(ds);
            } catch (Exception e) {
                log.error("Unexpected exception " + e.getMessage() + " in plugin " + nextPlugin.getClass().getName());
                pluginResultCode = DicomRouterPlugin.REQUEST_ABORT_PLUGIN_CHAIN;
            }
            
            switch (pluginResultCode) {
                
                case DicomRouterPlugin.CONTINUE:
                    // Process next plugin
                    chainResultCode = DicomRouterPlugin.CONTINUE;
                    break;
                    
                case DicomRouterPlugin.REQUEST_ABORT_PLUGIN_CHAIN:
                    chainResultCode = DicomRouterPlugin.CONTINUE;
                    break process_chain_block;
                    
                case DicomRouterPlugin.REQUEST_ABORT_RECEIVER:
                    chainResultCode = DicomRouterPlugin.REQUEST_ABORT_RECEIVER;
                    break process_chain_block;
                    
                default:
                    log.error("Undefined resultcode " + pluginResultCode + " from plugin " + nextPlugin.getClass().getName());
                    chainResultCode = DicomRouterPlugin.CONTINUE;
                    break process_chain_block;
            }
            
        }
        
        return chainResultCode;
    }
    
    
    /**
     * Closes all plugins
     */
    public void closePlugins() {
        DicomRouterPlugin   nextPlugin;
        
        // Process all plugins in sequence
        for (int i = 0; i < pluginVector.size(); i++) {
            nextPlugin = (DicomRouterPlugin) pluginVector.elementAt(i);
            
            nextPlugin.close();
        }
    }
    
}


/*
 * Revisions:
 *
 * 2006.04.25, hacklaender: Rewritten.
 */
