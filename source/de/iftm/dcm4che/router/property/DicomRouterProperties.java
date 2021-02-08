/*
 *  DicomRouterProperties.java
 *
 *  Copyright (c) 2003 by
 *
 *  VISUS Technology Transfer GmbH
 *  IFTM Institut für Telematik in der Medizn GmbH
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
package de.iftm.dcm4che.router.property;

import org.apache.log4j.*;

import org.dcm4che.data.*;

import de.iftm.dcm4che.router.util.*;

import java.util.*;
import java.io.*;
import java.net.*;


/**
 * This class has utilitie methods to access properties.
 *
 * @author Thomas Hacklaender
 * @version 2003.11.16
 */
public class DicomRouterProperties extends Properties {
    /** The logger for this plugin. */
    static Logger log = Logger.getLogger(DicomRouterProperties.class);
    
    /** Array holding indices of defined plugins*/
    private int[]   pluginIndices = null;

    
    /**
     * Creates a new empty instance of DicomRouterProperties.
     */
    public DicomRouterProperties() {
    }
    
    
    /**
     * Creates a new instance of DicomRouterProperties from other properties.
     * @param props the Properties to use.
     */
    public DicomRouterProperties(Properties props) {
        setProperties(props);
    }
    
    
    /**
     * Creates a new instance of DicomRouterProperties from a URI.
     *
     * @param uri the URI of file to load given as a string.
     */
    public DicomRouterProperties(String uri) throws IOException {
        this(Util.uriToFile(uri));
    }
    
    
    /**
     * Creates a new instance of DicomRouterProperties from a file.
     *
     * @param f the file to load.
     */
    public DicomRouterProperties(File f) throws IOException {
        this(new FileInputStream(f));
    }
    
    
    /**
     * Creates a new instance of DicomRouterProperties from a resource.
     * @param in stream of the resource to load.
     */
    public DicomRouterProperties(InputStream in) throws IOException {
        this.load(in);
    }
    
    
    /**
     * Sets the properties.
     *
     * @param props the properties to use.
     */
    public void setProperties(Properties props) {
        this.putAll(props);
        extractPluginIndices();
    }
    
    
    /**
     * Returns the properties of the receiver.
     *
     * @return the properties of the receiver.
     */
    public Properties extractReceiverProperties() {
        String leadin = "receiver.";
        return extractProperties(leadin);
    }
    
    
    /**
     * Returns the properties of a plugin.
     *
     * @param pluginNumber the number of the plugin.
     * @return the properties of the plugin.
     */
    public Properties extractPluginProperties(int pluginNumber) {
        String leadin = "plugin." + String.valueOf(pluginNumber) + ".";
        return extractProperties(leadin);
    }
    
    
    /**
     * Returns the properties whose keys start with leadin string
     *
     * @param leadin the lead in string.
     * @return the properties whose keys start with leadin string.
     */
    protected Properties extractProperties(String leadin) {
        Enumeration e = null;
        Properties  props = new Properties();
        String      key;
        String      value;
        String      newKey;
        
        e = this.keys();
        if ( e == null) return props;
        
        while (e.hasMoreElements()) {
            key = (String) e.nextElement();
            if (key.startsWith(leadin)) {
                newKey = key.substring(leadin.length());
                value = (String) this.get(key);
                props.setProperty(newKey, value);
            }
        }
        
        return props;
    }
    
    
    /**
     * Returns the number of plugins in the chain.
     * @return number of plugins.
     */
    public int getNumberOfPlugins() {
        if (pluginIndices == null) {
            return 0;
        } else {
            return pluginIndices.length;
        }
    }
    
    
    /**
     * Returns the index number of the first Plugin.
     * @return the index number of the plugin or -1 in the case of error.
     */
    public int getIndexNumberOfFirstPlugin() {
        if (pluginIndices == null) {
            return -1;
        } else {
            return pluginIndices[0];
        }
    }
    
    
    /**
     * Returns the index number of the last Plugin.
     * @return the index number of the plugin or -1 in the case of error.
     */
    public int getIndexNumberOfLastPlugin() {
        if (pluginIndices == null) {
            return -1;
        } else {
            return pluginIndices[pluginIndices.length -1];
        }
    }
     
    /**
     * Get the array holding the ascending sorted indices of defined plugins.
     */
    public int[] getPluginIndexNumbers() {
        return pluginIndices;
    }
   
    
    /**
     *
     */
    protected void extractPluginIndices() {
        String          key;
        StringTokenizer st;
        String          token;
        Enumeration     en = this.keys();
        Vector          vectorOfIndices = new Vector();
        
        while (en.hasMoreElements()) {
            key = (String) en.nextElement();
            if (key.startsWith("plugin.") && key.endsWith(".name")) {
                st = new StringTokenizer(key, ".");
                if (st.hasMoreTokens()) {
                    // discharge "plugin"
                    token = st.nextToken();
                    if (st.hasMoreTokens()) {
                        // get plugin index
                        token = st.nextToken();
                        try {
                            vectorOfIndices.addElement(Integer.valueOf(token));
                        } catch (NumberFormatException e) {}
                    }
                }
            }
        }
        
        // Vector contains the plugin indices
        pluginIndices = new int[vectorOfIndices.size()];
        for (int i = 0; i < vectorOfIndices.size(); i++) {
            pluginIndices[i] = ((Integer) vectorOfIndices.elementAt(i)).intValue();
        }
        Arrays.sort(pluginIndices);
    }
    
}

/**
 * Revisions:
 *
 * 2006.04.24, hacklaender: Clean up.
 * 2006.02.23, hacklaender: Several methods added.
 * 2005.08.16: Class PropertyUtil renamed to DicomRouterProperties
 * 2005.08.16: Class DicomRouterProperties extends Properties
 */
