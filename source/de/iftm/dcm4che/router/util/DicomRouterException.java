/*
 *  DicomReceiverException.java
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
package de.iftm.dcm4che.router.util;


/**
 *
 * @author  hacklaender
 */
public class DicomRouterException  extends Exception {
    
    public static final int UNSPECIFIED_ERROR = 0;
    public static final int NO_PLUGINS_SPECIFIED = 1;
    public static final int PLUGIN_INDEX_SYNTAX_ERROR = 2;
    public static final int NO_PROPERTIES_FOR_PLUGIN = 3;
    
    private int theID;

    
    /**
     * Creates a new instance of DicomRouterException.
     */
    public DicomRouterException() {
        super();
        theID = UNSPECIFIED_ERROR;
    }
    
    
    /**
     * Creates a new instance of DicomRouterException.
     */
    public DicomRouterException(int id) {
        super();
        theID = id;
    }
    
    
    /**
     * Creates a new instance of DicomRouterException.
     */
    public String getMessage() {
        switch (theID) {
            
            case NO_PLUGINS_SPECIFIED:
                return "No plugins specified";
             
            case PLUGIN_INDEX_SYNTAX_ERROR:
                return "Plugin index syntax error";
            
            case NO_PROPERTIES_FOR_PLUGIN:
                return "No properties specified for plugin";
               
            case UNSPECIFIED_ERROR:
            default:
                return "Unspecified error";
                
        }
    }
    
    
    /**
     * Returns the event ID (type).
     */
    public int getID() {
        return theID;
    }
}
