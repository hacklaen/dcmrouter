/*
 *  Copyright (c) 2006 by
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

import org.dcm4che.data.Dataset;

import org.apache.log4j.*;

/**
 * @author hacklaender
 * @version 2006.04.27
 */
public class DicomRouterEvent extends java.util.EventObject {
    
    /** Initialize logger */
    private static Logger log = Logger.getLogger(DicomRouterEvent.class);
    
    public static final int STARTING_EVENT = 0;
    public static final int NEXT_OBJECT_EVENT = 1;
    public static final int FINISHED_EVENT = 2;
    public static final int PROGRESS_EVENT = 3;
    
    /** The type of the event. */
    private int eventID;
    
    /** The DICOM object to process next. Only valid for NEXT_OBJECT_EVENT events. */
    private Dataset dicomObject;
    
    /** The actual index of the processed object in the list of all objects to
     * process. Only valid for PROGRESS_EVENT events. */
    private int actualValue;
    
    /** The number of objects in the list of objects to process. Only valid for
     * PROGRESS_EVENT events. */
    private int maxValue;
    
    
    /**
     * Creates a new instance of DicomRouterEvent
     */
    public DicomRouterEvent(Object source) {
        // pass the source object to the supercalass
        super(source);
    }
    
    
    /**
     * Returns the event ID.
     *
     * @return the event ID.
     */
    public int getID() {
        return eventID;
    }
    
    
    /**
     * Returns the actual index of the processed object in the list of all objects
     * to process.
     *
     * @return the actual value.
     */
    public int getActualValue(){
        return actualValue;
    }
    
    
    /**
     * Returns the number of objects in the list of objects to process.
     *
     * @return the maximum value.
     */
    public int getMaxValue() {
        return maxValue;
    }
    
    
    /**
     * Returns the next DICOM object to process.
     *
     * @return the DICOM object to process.
     */
    public Dataset getDicomObject() {
        return dicomObject;
    }
    
    
    /**
     * This event should be send before processing the first DICOM object.
     */
    public void setStartingEvent() {
        eventID = STARTING_EVENT;
    }
    
    
    /**
     * This event should be send for each DICOM object before processing the 
     * object in the plugin chain.
     * @param ds the next DICOM object to process as a Dataset.
     */
    public void setNextObjectEvent(Dataset ds) {
        eventID = NEXT_OBJECT_EVENT;
        dicomObject = ds;
    }
    
    
    /**
     * This event should be send after processing the last DICOM object.
     */
    public void setFinishedEvent() {
        eventID = FINISHED_EVENT;
    }
    
    
    /**
     * If the total number of objects to process is known, this event should be 
     * send for each DICOM object before processing the object in the plugin chain.
     * @param actual the actual index of the processed object in the list of all
     * objects to process.
     * @param max the number of objects in the list of objects to process.
     */
    public void setProgressEvent(int actual, int max) {
        eventID = PROGRESS_EVENT;
        actualValue = actual;
        maxValue = max;
    }
    
}
